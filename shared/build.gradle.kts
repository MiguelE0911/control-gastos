plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(21)

    // Paso 1 de la Fase 2: solo target JVM (Desktop).
    // android() e ios() se añadirán en el Paso 2.
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
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
