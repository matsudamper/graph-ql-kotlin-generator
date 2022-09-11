import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.matsudamper.graphql.generator.gradle.GenerateQlTask

plugins {
    kotlin("jvm") version "1.7.10"
    id("net.matsudamper.graphql.generator") version "1.0-SNAPSHOT"
}

group = "net.matsudamper.graphql.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("net.matsudamper.graphql.generator:lib:1.0-SNAPSHOT")
    implementation("com.graphql-java:graphql-java:19.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val generatedPath = "src/main/kotlin_generated"
sourceSets["main"].java.setSrcDirs(
    listOf(
        "src/main/kotlin",
        generatedPath,
    ).map { File(it) }
)
val generateGraphqlCodeTask = tasks.create<GenerateQlTask>("generateGraphqlCode") {
    files = file("$projectDir/src/main/graphqls").listFiles().orEmpty()
        .filter { it.extension == "graphqls" }
    packageName = "net.matsudamper.graphql.server.generated"
    outDir = file(generatedPath)
    scalarMap.putAll(
        mapOf(
            "UserId" to "kotlin.Long",
            "UserPostContentId" to "kotlin.Long",
            "JvmInt" to "kotlin.Int",
            "JvmFloat" to "kotlin.Float",
            "JvmDouble" to "kotlin.Double",
            "Date" to "java.util.Date",
        )
    )
}

tasks.withType<KotlinCompile> {
    dependsOn(generateGraphqlCodeTask)
}