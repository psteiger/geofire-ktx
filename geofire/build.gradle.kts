plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}

android {
    compileSdk = 31
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.firebase:geofire-android:3.1.0")
    implementation(platform("com.google.firebase:firebase-bom:29.1.0"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.github.psteiger:firebase-database-ktx:0.2.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs +=
        "-Xopt-in=" +
                "kotlin.RequiresOptIn"
}

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            register("release", MavenPublication::class) {
                from(components["release"])
                artifactId = project.name
            }
        }
    }
}
