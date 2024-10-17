import com.android.build.api.variant.FilterConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

apply(from = "../secret.gradle.kts")

android {
    namespace = "com.skyd.anivu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skyd.anivu"
        minSdk = 24
        targetSdk = 35
        versionCode = 24
        versionName = "2.1-beta07"

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
    packaging {
        resources.excludes += mutableSetOf(
            "DebugProbesKt.bin",
            "META-INF/CHANGES",
            "META-INF/README.md",
            "META-INF/jdom-info.xml",
            "kotlin-tooling-metadata.json",
            "okhttp3/internal/publicsuffix/NOTICE",
            "rome-utils-*.jar",
        )
        jniLibs {
            excludes += mutableSetOf(
                "lib/*/libffmpegkit.so",                // mpv-android
                "lib/*/libffmpegkit_abidetect.so",      // mpv-android
            )
            useLegacyPackaging = true
        }
        dex {
            useLegacyPackaging = true
        }
    }
    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
//    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
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
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
            "-opt-in=kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi"
        )
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.window.size)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.material)
    implementation(libs.material.kolor)
    implementation(libs.accompanist.permissions)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.okhttp3)
    implementation(libs.okhttp3.coroutines.jvm)
    implementation(libs.okhttp3.logging)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.aniyomi.mpv.lib)
    implementation(libs.ffmpeg.kit)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)

    implementation(libs.lottie.compose)

    implementation(libs.rome)
    implementation(libs.rome.modules)
    implementation(libs.ceau.opmlparser) {
        exclude(group = "net.sf.kxml", module = "kxml2")
    }

    implementation(libs.readability4j)

    implementation(libs.reorderable)

    implementation(libs.libtorrent4j.arm64)
    implementation(libs.libtorrent4j.arm)
    implementation(libs.libtorrent4j.x86)
    implementation(libs.libtorrent4j.x8664)

//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}