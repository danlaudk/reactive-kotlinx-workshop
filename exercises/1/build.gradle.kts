import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

group = "com.ordina"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<KotlinCompile>().configureEach() { //
    kotlinOptions {
        jvmTarget = "17"  // ?
        freeCompilerArgs += "-Xcontext-receivers"
    }
}

application {
    mainClass.set("MainKt")
}

