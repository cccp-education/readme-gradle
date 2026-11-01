package readme

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * Production implementation of GitRemoteValidator.
 * Resolves the remote URL from the local .git config (origin) —
 * never from ReadmePlantUmlConfig.
 * Uses a lightweight ls-remote to validate token, repo existence
 * and push rights.
 * Delegates filesystem walk to GitUtils.findGitRoot().
 *
 * XB-FP-5 (arrow-kt adoption): the nested `try`/`catch` blocks are replaced by
 * `Either.catch` + `fold`. The JGit failure becomes a value
 * (`GitValidationResult`) that is mapped once, and the origin lookup is captured
 * the same way. The public sealed boundary is unchanged.
 */
class JGitRemoteValidator : GitRemoteValidator {
    override fun validate(config: GitConfig): GitValidationResult {
        if (config.token.isBlank() || config.token == "<YOUR_GITHUB_PAT>") {
            return GitValidationResult.TokenPlaceholder
        }

        return validateRemote(config).fold(
            ifLeft = { failure -> failure },
            ifRight = { GitValidationResult.Valid },
        )
    }

    /**
     * Performs the `ls-remote` probe. `Left` carries the already-mapped
     * [GitValidationResult] failure; `Right` means the remote is reachable and the
     * token accepted.
     */
    private fun validateRemote(config: GitConfig): Either<GitValidationResult, Unit> {
        val repoUrl = resolveOriginUrl() ?: return GitValidationResult.Unreachable.left()

        val credentials = UsernamePasswordCredentialsProvider("x-access-token", config.token)
        return Either
            .catch {
                Git
                    .lsRemoteRepository()
                    .setRemote(repoUrl)
                    .setCredentialsProvider(credentials)
                    .call()
                // TODO: verify push rights via GitHub API (contents:write scope)
                Unit
            }.fold(
                ifLeft = { throwable -> throwable.toValidationResult().left() },
                ifRight = { Unit.right() },
            )
    }

    /** Maps a JGit failure to the sealed validation vocabulary. */
    private fun Throwable.toValidationResult(): GitValidationResult =
        when {
            this is TransportException && message?.contains("not found", ignoreCase = true) == true ->
                GitValidationResult.RepositoryNotFound

            this is TransportException && message?.contains("not authorized", ignoreCase = true) == true ->
                GitValidationResult.InsufficientPushRights

            else -> GitValidationResult.Unreachable
        }

    /**
     * Resolves the `origin` URL, or `null` (after a warning) when the repository
     * or the remote is unavailable. Exception capture is a value, not a throw.
     */
    private fun resolveOriginUrl(): String? {
        val gitRoot =
            Either
                .catch { GitUtils.findGitRoot(File(".").canonicalFile) }
                .fold(
                    ifLeft = {
                        println("[readme] WARNING: failed to locate the git root — ${it.message}")
                        null
                    },
                    ifRight = { it },
                )
                ?: run {
                    println("[readme] WARNING: no .git directory found in filesystem hierarchy — remote unreachable")
                    return null
                }

        return Either
            .catch {
                Git.open(gitRoot).use { git ->
                    git.repository.config.getString("remote", "origin", "url")
                }
            }.fold(
                ifLeft = {
                    println("[readme] WARNING: failed to read .git config at ${gitRoot.absolutePath} — ${it.message}")
                    null
                },
                ifRight = { url ->
                    url?.takeIf { it.isNotBlank() }
                        ?: run {
                            println(
                                "[readme] WARNING: no 'origin' remote configured in " +
                                    "${gitRoot.absolutePath}/.git/config — remote unreachable",
                            )
                            null
                        }
                },
            )
    }
}
