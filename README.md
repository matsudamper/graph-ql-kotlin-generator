# GraphQL Kotlin Generator
Genrate Kotlin code for [graphql-java](https://github.com/graphql-java/graphql-java) from schema file.

# How to use

<details>
<summary>build.gradle.kts</summary>

```kotlin
plugins {
    id("net.matsudamper.graphql.generator") version "<version>"
}
dependencies {
    implementation("net.matsudamper.graphql.generator:lib:<version>")
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/matsudamper/graphql-kotlin-generator")
        credentials {
            username = "user_name"
            password = System.getenv("GITHUB_TOKEN") // require read:packages Permission
        }
    }
}

val generatedPath = "build/generated/graphql/main/kotlin"
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
    dependsOn(generateGraphqlCodeTask)
}
```

</details>

<details>
<summary>settings.gradle</summary>

```
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/matsudamper/graphql-kotlin-generator")
            credentials {
                it.username = "user_name"
                it.password = System.getenv("GITHUB_TOKEN") // require read:packages Permission
            }
        }
        gradlePluginPortal()
    }
}
```
</details>
