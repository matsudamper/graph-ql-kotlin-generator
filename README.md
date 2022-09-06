# Usage
```kotlin
plugins {
    id("net.matsudamper.graphql.generator") version "<version>"
}
dependencies {
    implementation("net.matsudamper.graphql.generator:lib:<version>")
}

val generateGraphqlCodeTask = tasks.create<net.matsudamper.graphql.generator.gradle.GenerateQlTask>("generateGraphqlCode") {
    files = file("$projectDir/src/main/resources/graphql").listFiles().orEmpty()
        .filter { it.extension == "graphqls" }
    packageName = "base.package.name"
    outDir = file(generatedPath)
    scalarMap.putAll(
        mapOf(
            "UserId" to "kotlin.Long",
            "JvmInt" to "kotlin.Int",
            "JvmFloat" to "kotlin.Float",
            "JvmDouble" to "kotlin.Double",
            "Date" to "java.util.Date",
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateQlTask)
}
```
