plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "net.matsudamper.graphql.generator"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

sourceSets["main"].java.setSrcDirs(
    listOf(
        "src/main/kotlin",
    ).map { File(it) }
)

tasks.create("publishToLocal") {
    finalizedBy("publishToMavenLocal")
    finalizedBy(":lib:publishToMavenLocal")
}

dependencies {
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")


    implementation("com.graphql-java:graphql-java:17.3")
    implementation("com.graphql-java:graphql-java-extended-validation:17.0")
    implementation("com.graphql-java:graphql-java-extended-scalars:17.0")

    implementation("com.squareup:kotlinpoet:1.10.2") {
        exclude(module = "kotlin-reflect")
    }
}

java {
    withSourcesJar()
}

gradlePlugin {
    (plugins) {
        "net.matsudamper.graphql.generator.gradle.QlGradle" {
            id = "net.matsudamper.graphql.generator"
            version = project.version
            implementationClass = "net.matsudamper.graphql.generator.gradle.QlGradlePlugin"
        }
    }
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes", "-Xjsr305=strict", "-Xexplicit-api=warning")
}