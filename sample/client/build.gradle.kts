import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.apollographql.apollo3.gradle.internal.ApolloDownloadSchemaTask

plugins {
    id("com.apollographql.apollo3") version "3.5.0"
    kotlin("jvm") version "1.7.10"
}

group = "net.matsudamper.graphql.client"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val apolloVersion = "3.5.0"
    implementation("com.apollographql.apollo3:apollo-runtime:$apolloVersion")
    implementation("com.apollographql.apollo3:apollo-normalized-cache:$apolloVersion")
    implementation("com.apollographql.apollo3:apollo-adapters:$apolloVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


tasks.getByName<ApolloDownloadSchemaTask>(
    "downloadApolloSchema"
) {
    endpoint.set("http://localhost/schema")
    schema.set(file("src/main/graphql/schema.graphqls").canonicalPath)
}

apollo {
    schemaFiles.setFrom(
        listOf(
            File("src/main/graphql/schema.graphqls"),
        )
    )

    generateOptionalOperationVariables.set(false)
    generateKotlinModels.set(true)
    packageName.set("net.matsudamper.graphql.client.generated")

    mapScalar("UserId", "kotlin.Long")
    mapScalar("UserPostContentId", "kotlin.Long")
    mapScalar("JvmInt", "kotlin.Int")
    mapScalar("JvmFloat", "kotlin.Float")
    mapScalar("JvmDouble", "kotlin.Double")
    mapScalar("Date", "java.util.Date", "com.apollographql.apollo3.adapter.DateAdapter")
}
