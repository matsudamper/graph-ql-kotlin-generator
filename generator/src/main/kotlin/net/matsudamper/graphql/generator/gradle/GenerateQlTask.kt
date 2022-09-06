package net.matsudamper.graphql.generator.gradle

import net.matsudamper.graphql.generator.Main
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File


public abstract class GenerateQlTask : DefaultTask() {
    @get:InputFiles
    public lateinit var files: List<File>

    @get:InputFiles
    public lateinit var packageName: String

    @get:InputFiles
    public lateinit var outDir: File

    @get:InputFiles
    public val scalarMap: MutableMap<String, String> = mutableMapOf()

    @TaskAction
    private fun exec() {
        println("GenerateQlTask.exec()")
        println("files -> $files")
        Main(
            basePackage = packageName,
            schemaFiles = files,
            outDir = outDir,
            scalarMap = scalarMap,
        ).generate()
    }
}