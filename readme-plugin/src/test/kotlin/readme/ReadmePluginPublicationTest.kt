package readme

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.text.Charsets.UTF_8

/**
 * MEM-CAT-ROLLOUT-6 (S-212, cross-borough MEMPHIS) — publication hygiene guard.
 *
 * D3: the plugin self version is derived from the published workspace catalog
 * (`ws.versions.readme.plugin.get()`) — the ws catalog is the cross-borough
 * source of truth.
 * D4: the borough pins the catalog once in settings.gradle.kts.
 * D5 hygiene: the local toml self version and the ws catalog version must agree.
 */
class ReadmePluginPublicationTest {
    private val pluginDir = File(System.getProperty("user.dir")).absoluteFile

    private val rootDir =
        pluginDir.parentFile
            ?: throw IllegalStateException("Cannot resolve repo root from plugin dir")

    @Test
    fun `plugin version matches ws catalog version`() {
        val buildScript = pluginDir.resolve("build.gradle.kts").readText(UTF_8)
        // S-327 (MEM-LINT-ADOPTION) — ktlint reflows the assignment across several
        // lines, so the guard compares the declaration whitespace-insensitively
        // instead of matching a single source line (S-244/S-325 F34 trap).
        val versionDeclaration =
            buildScript
                .lines()
                .dropWhile { !it.trimStart().startsWith("version =") }
                .takeWhile { it.isNotBlank() }
                .joinToString("")
                .replace(Regex("\\s+"), "")

        // MEM-CAT-ROLLOUT-6 (D3) — self version derived from the published workspace catalog.
        assertThat(versionDeclaration)
            .withFailMessage("build.gradle.kts version must derive from the published workspace catalog (ws.versions.readme.plugin)")
            .contains("ws.versions.readme.plugin.get()")

        // Hygiene (D5): local toml self version must match the ws catalog version —
        // the ws catalog (workspace-bom repo) is the cross-borough source of truth.
        val pluginCatalogVersion = readmeVersionFrom(pluginDir.resolve("gradle/libs.versions.toml").readText(UTF_8))
        // Injected by Gradle from the pinned published catalog (CI has no sibling checkout).
        val wsCatalogVersion =
            System.getProperty("readme.publishedCatalog.version")
                ?: readmeVersionFrom(wsCatalogToml())

        assertThat(pluginCatalogVersion)
            .withFailMessage("plugin catalog readme version ($pluginCatalogVersion) must match ws catalog readme-plugin version ($wsCatalogVersion)")
            .isEqualTo(wsCatalogVersion)
    }

    @Test
    fun `settings pins the workspace catalog`() {
        val settings = pluginDir.resolve("settings.gradle.kts").readText(UTF_8)

        // CNV-17 — single JitPack channel: the catalog is served by JitPack.
        assertThat(settings)
            .withFailMessage("settings.gradle.kts must pin the JitPack workspace catalog")
            .contains("""from("com.github.cccp-education.workspace-bom:workspace-catalog:""")
    }

    @Test
    fun `plugin group and id are stable for publication`() {
        val buildScript = pluginDir.resolve("build.gradle.kts").readText(UTF_8)

        // The plugin id is declared inline in the gradlePlugin block (gradlePlugin.plugins readme).
        val idLine =
            buildScript
                .lineSequence()
                .first { it.trimStart().startsWith("id ") && it.contains("education.cccp.readme") }

        assertThat(buildScript).contains("group = \"education.cccp\"")
        assertThat(idLine.substringAfter("\"").substringBefore("\"")).isEqualTo("education.cccp.readme")
    }

    /**
     * Reads the `ws` catalog toml and extracts the `readme-plugin` version.
     * Fallback: parse the local MEMPHIS repo toml (same source file as the
     * published catalog).
     */
    private fun wsCatalogToml(): String {
        val wsRepoToml =
            rootDir.parentFile
                ?.resolve("workspace-bom/gradle/libs.versions.toml")
        if (wsRepoToml != null && wsRepoToml.exists()) return wsRepoToml.readText(UTF_8)
        error("ws catalog toml introuvable — résolution ws impossible pour l'hygiène")
    }

    private fun readmeVersionFrom(content: String): String =
        content
            .lineSequence()
            .map { it.substringBefore('#').trim() }
            .first { it.startsWith("readme-plugin =") || it.startsWith("readme =") }
            .substringAfter("\"")
            .substringBefore("\"")
}
