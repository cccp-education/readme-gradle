package readme

import org.gradle.api.Plugin
import org.gradle.api.Project

class ReadmePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val config = ReadmePlantUmlConfig.load(project.projectDir)

        // imgDir is always resolved from the git root — not from the Gradle project dir.
        // This ensures .github/workflows/readmes/images/ lands at the repository root
        // even when the Gradle project lives in a subdirectory.
        val imgDir = GitUtils.resolveImgDir(project.projectDir, config.output.imgDir)

        val generateReadme = project.tasks.register(
            "generateReadme",
            ScaffoldTask::class.java
        ) { task ->
            task.group       = "generate"
            task.description = "Creates readme.yml and .github/workflows/readme_action.yml if absent"
            task.projectDir  .set(project.layout.projectDirectory)
        }

        val transformReadme = project.tasks.register(
            "transformReadme",
            ProcessReadmeTask::class.java
        ) { task ->
            task.group       = "transform"
            task.description = "Generate README*.adoc and images from README_truth*.adoc sources"

            task.sourceDir  .set(project.layout.projectDirectory.dir(config.source.dir))
            task.imgDir     .set(project.layout.dir(project.provider { imgDir }))
            task.buildImgDir.set(project.layout.buildDirectory.dir("img"))
            task.defaultLang.set(config.source.defaultLang)

            task.dependsOn(generateReadme)
        }

        project.tasks.register(
            "commitGeneratedReadme",
            CommitGeneratedReadmeTask::class.java
        ) { task ->
            task.group       = "deploy"
            task.description = "Commits and pushes README*.adoc generated via JGit (CI only)"

            task.repoDir      .set(project.layout.projectDirectory)
            task.gitUserName  .set(config.git.userName)
            task.gitUserEmail .set(config.git.userEmail)
            task.commitMessage.set(config.git.commitMessage)
            task.gitToken     .set(project.provider { config.git.resolvedToken() })

            task.dependsOn(transformReadme)
        }
    }
}
