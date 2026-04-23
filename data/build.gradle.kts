plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amro.data"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        val tmdbBaseUrl = providers.gradleProperty("TMDB_BASE_URL")
            .orNull
            ?: "https://api.themoviedb.org/3/"

        val tmdbBearerToken = providers.gradleProperty("TMDB_BEARER_TOKEN").orNull ?: ""
        val allowMissingToken = providers.gradleProperty("ALLOW_MISSING_TMDB_TOKEN")
            .map(String::toBoolean)
            .orElse(false)
            .get()

        val enforceTokenForTasks = gradle.startParameter.taskNames.any { taskName ->
            val t = taskName.lowercase()
            t.contains("assemble") || t.contains("install") || t.contains("bundle")
        }
        if (enforceTokenForTasks && !allowMissingToken && tmdbBearerToken.isBlank()) {
            throw GradleException(
                "Missing TMDB bearer token. Set TMDB_BEARER_TOKEN in your Gradle properties.\n" +
                    "Example (user-level gradle.properties):\n" +
                    "TMDB_BEARER_TOKEN=<<TMDB API Read Access Token>>\n" +
                    "If you *really* need to bypass locally, set ALLOW_MISSING_TMDB_TOKEN=true."
            )
        }

        buildConfigField("String", "TMDB_BASE_URL", "\"$tmdbBaseUrl\"")
        buildConfigField("String", "TMDB_BEARER_TOKEN", "\"$tmdbBearerToken\"")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":domain"))
    implementation(project(":core"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
}