import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    jvmToolchain(21)

    // Target JVM: Desktop (JavaFX).
    jvm()

    // Target Android: biblioteca KMP (sin app todavía).
    // iOS queda suspendido hasta contar con macOS.
    android {
        namespace = "com.miguel.gastos.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
    }
}

sqldelight {
    databases {
        create("GastosDatabase") {
            packageName.set("com.miguel.gastos.db")
        }
    }
}
