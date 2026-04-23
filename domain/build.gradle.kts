plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.javax.inject)
}

kotlin {
    jvmToolchain(11)
}