import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

fun backendSetting(environmentName: String, propertyName: String): String =
    System.getenv(environmentName)
        ?: localProperties.getProperty(propertyName, "")

android {
    namespace = "com.example.refiner_app"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        applicationId = "com.example.refiner_app"
        minSdk = maxOf(flutter.minSdkVersion, 23)
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName

        resValue(
            "string",
            "refiner_backend_url",
            backendSetting("REFINER_BACKEND_URL", "refiner.backendUrl"),
        )
        resValue(
            "string",
            "refiner_backend_key",
            backendSetting("REFINER_BACKEND_KEY", "refiner.backendKey"),
        )
    }

    buildTypes {
        release {
            // Replace this debug signing configuration before a Play Store release.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}