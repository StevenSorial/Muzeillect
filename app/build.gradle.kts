@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  val apiLevel = 36
  compileSdkVersion(apiLevel)
  namespace = "com.steven.muzeillect"
  ndkVersion = "29.0.14206865"

  defaultConfig {
    applicationId = "com.steven.muzeillect"
    minSdk = 23
    targetSdk = apiLevel
    versionCode = 25
    versionName = "4.0"
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
  }

  bundle {
    density.enableSplit = true
    abi.enableSplit = true
    language.enableSplit = false
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      ndk {
        debugSymbolLevel = "FULL"
      }
    }

    debug {
      isMinifyEnabled = false
      isShrinkResources = false
      applicationIdSuffix = ".debug"
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

    all {
      val completeId = "${defaultConfig.applicationId}${applicationIdSuffix ?: ""}"
      manifestPlaceholders["appBundle"] = completeId
    }

    buildFeatures {
      buildConfig = true
      compose = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_11
    }
  }
}

dependencies {
  implementation(fileTree("libs") { include("*.jar") })

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose)
  implementation(libs.androidx.compose.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.fonts)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.work.runtime)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.material.material)

  implementation(libs.timber)

  implementation(libs.okhttp.okhttp)
  implementation(libs.okhttp.tls)
  implementation(libs.okhttp.coroutines)

  implementation(libs.jsoup)
  implementation(libs.muzei.api)
  implementation(libs.coil.compose)
  implementation(libs.coil.okhttp)

  debugImplementation(libs.leakcanary.android)
}
