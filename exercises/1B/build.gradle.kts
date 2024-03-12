plugins {
    id("com.ordina.kotlin-conventions")
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
