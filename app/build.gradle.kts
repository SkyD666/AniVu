import com.android.build.api.variant.FilterConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

apply(from = "../secret.gradle.kts")

android {
    namespace = "com.skyd.anivu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skyd.anivu"
        minSdk = 24
        targetSdk = 34
        versionCode = 16
        versionName = "1.1-beta18"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        create("release") {
            @Suppress("UNCHECKED_CAST")
            val sign = ((extra["secret"] as Map<*, *>)["sign"] as Map<String, String>)
            storeFile = file("../key.jks")
            storePassword = sign["RELEASE_STORE_PASSWORD"]
            keyAlias = sign["RELEASE_KEY_ALIAS"]
            keyPassword = sign["RELEASE_KEY_PASSWORD"]
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("GitHub") {
            dimension = "version"
        }
    }

    splits {
        abi {
            // Enables building multiple APKs per ABI.
            isEnable = true
            // By default all ABIs are included, so use reset() and include().
            // Resets the list of ABIs for Gradle to create APKs for to none.
            reset()
            // A list of ABIs for Gradle to create APKs for.
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            // We want to also generate a universal APK that includes all ABIs.
            isUniversalApk = true
        }
    }

    applicationVariants.all {
        outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val abi = output.getFilter(FilterConfiguration.FilterType.ABI.name) ?: "universal"
                output.outputFileName =
                    "AniVu_${versionName}_${abi}_${buildType.name}_${flavorName}.apk"
            }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.getByName("release")    // signing
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources.excludes += mutableSetOf(
            "DebugProbesKt.bin",
            "META-INF/CHANGES",
            "META-INF/README.md",
            "META-INF/jdom-info.xml",
            "kotlin-tooling-metadata.json",
            "okhttp3/internal/publicsuffix/NOTICE",
        )
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.ui:ui:1.6.6")
    implementation("androidx.compose.material:material:1.6.6")
    implementation("androidx.compose.material3:material3:1.3.0-alpha05")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.6")
    implementation("com.materialkolor:material-kolor:1.4.4")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.3.0-beta01")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.android.material:material:1.11.0")

    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-coroutines-jvm:5.0.0-alpha.12")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    implementation("com.rometools:rome:2.1.0")
    implementation("net.dankito.readability4j:readability4j:1.0.8")

    implementation("org.libtorrent4j:libtorrent4j-android-arm64:2.1.0-31")
    implementation("org.libtorrent4j:libtorrent4j-android-arm:2.1.0-31")
    implementation("org.libtorrent4j:libtorrent4j-android-x86:2.1.0-31")
    implementation("org.libtorrent4j:libtorrent4j-android-x86_64:2.1.0-31")

//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}