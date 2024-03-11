plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "reactive-kotlinx-workshop"

include("exercises:1A")
//include("exercises:1B")

val logbackVersion: String = "1.4.14"
val exposedVersion: String = "0.47.0"
val arrowVersion: String = "1.2.1"
val kotestVersion: String = "5.8.0"
val coroutinesVersion = "1.8.0"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("logback", "ch.qos.logback:logback-classic:$logbackVersion")
            bundle("logging", listOf("logback"))

            library("exposed-core", "org.jetbrains.exposed:exposed-core:$exposedVersion")
            library("exposed-dao", "org.jetbrains.exposed:exposed-dao:$exposedVersion")
            library("exposed-jdbc", "org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
            library("exposed-java-time", "org.jetbrains.exposed:exposed-java-time:$exposedVersion")
            library("hikari", "com.zaxxer:HikariCP:5.0.1")
            library("postgresql", "org.postgresql:postgresql:42.6.0")

            bundle(
                "database",
                listOf("exposed-core", "exposed-dao", "exposed-jdbc", "exposed-java-time", "hikari", "postgresql")
            )

            library("arrow", "io.arrow-kt:arrow-core:1.2.1")


            library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            library("coroutines-slf4j", "org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")

            bundle("coroutines", listOf("coroutines-core", "coroutines-slf4j"))
        }
    }
}
