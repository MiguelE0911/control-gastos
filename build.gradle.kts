plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.javafx) apply false
}

allprojects {
    group = "com.miguel.gastos"
    version = "1.0-SNAPSHOT"
}
