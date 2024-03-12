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


val coroutinesVersion = "1.8.0"
val arrowFxVersion = "1.2.1"

dependencies {
    implementation("io.klogging:klogging-jvm:0.5.11")

    implementation(libs.bundles.coroutines)

    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowFxVersion")

    implementation("io.arrow-kt:arrow-fx-stm:$arrowFxVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
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

