plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.ordina"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
