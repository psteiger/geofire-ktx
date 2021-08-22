plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}

android {
    compileSdk = 30
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    coroutines()
    geofire()
    firebaseDatabase()
    firebaseDatabaseKtx()
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

fun DependencyHandlerScope.coroutines() {
    val version = "1.5.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version")
}

fun DependencyHandlerScope.geofire() {
    val version = "3.0.0"
    implementation("com.firebase:geofire-android:$version")
}

fun DependencyHandlerScope.firebaseDatabase() {
    val version = "28.3.1"
    implementation(platform("com.google.firebase:firebase-bom:$version"))
    implementation("com.google.firebase:firebase-database-ktx")
}

fun DependencyHandlerScope.firebaseDatabaseKtx() {
    val version = "0.1.8"
    implementation("com.github.psteiger:firebase-database-ktx:$version")
}