package net.matsudamper.graphql.generator.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class QlGradle : Plugin<Project> {
    override fun apply(projict: Project) {
        projict.run {
            tasks.whenTaskAdded {
                when (val currentTask = this) {
                    is GenerateQlTask -> {
                        tasks.withType<Assemble> {
                            dependsOn(currentTask)
                        }
                    }
                }
            }
        }
    }
}
apply<QlGradle>()
