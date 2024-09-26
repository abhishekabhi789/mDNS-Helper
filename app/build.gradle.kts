plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.dagger.hilt.android)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "io.github.abhishekabhi789.mdnshelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.abhishekabhi789.mdnshelper"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val customIntentActions = mutableMapOf(
            Pair("ACTION_SHORTCUT_LAUNCH", "$applicationId.action.LAUNCH_SHORTCUT"),
            Pair("ACTION_SHORTCUT_ADDED_PINNED", "$applicationId.action.ADDED_PINNED_SHORTCUT")
        )
        manifestPlaceholders.putAll(customIntentActions)
        customIntentActions.forEach {
            buildConfigField("String", it.key, "\"${it.value}\"")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //dagger hilt di
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)
    //dnsssd
    implementation(libs.rx2dnssd)
    //crop images for icon
    implementation(libs.android.image.cropper)
    //material icons extended
    implementation(libs.androidx.material.icons.extended)
    //custom tab
    implementation(libs.androidx.browser)
    //liceses
    implementation(libs.play.services.oss.licenses)
}
kapt {
    correctErrorTypes = true
}
