package readme

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'education.cccp.readme' plugin.
 */
class CanaryReadmeGradlePluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("education.cccp.readme")

        // Verify the result
        assertNotNull(project.tasks.findByName("transformReadme"))       // ← tâche réelle
        assertNotNull(project.tasks.findByName("generateReadme"))      // ← tâche réelle
//        assertNotNull(project.tasks.findByName("tasks"))
    }
}
