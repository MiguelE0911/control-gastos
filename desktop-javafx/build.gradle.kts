plugins {
    java
    application
    alias(libs.plugins.javafx)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "com.miguel.gastos.MainApp"
}

dependencies {
    // Módulo compartido (KMP JVM)
    implementation(project(":shared"))
    // Necesario para compilar contra kotlinx-datetime desde Java
    implementation(libs.kotlinx.datetime)

    // Reportes PDF (desktop)
    implementation(libs.jasperreports) {
        exclude(group = "com.github.librepdf", module = "openpdf")
    }
    implementation(libs.openpdf)
}
