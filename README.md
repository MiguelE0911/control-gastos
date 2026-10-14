# Control de Gastos Personales

Aplicación de escritorio para el control de gastos personales.

## Tecnologías

- **Java 21** + **Kotlin Multiplatform (JVM target)**
- **JavaFX 21** — Interfaz gráfica
- **SQLDelight** — Persistencia SQLite (archivo `~/.control-gastos/control_gastos.db`)
- **Gradle Kotlin DSL** — Build
- **JasperReports / OpenPDF** — Generación de reportes PDF

## Estructura del proyecto

```
control-gastos/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── shared/             # Módulo KMP (commonMain + jvmMain)
│   ├── src/commonMain/kotlin/com/miguel/gastos/
│   │   ├── model/
│   │   ├── service/   # ReporteCalculator (lógica pura)
│   │   └── data/      # GastoRepository
│   └── src/jvmMain/kotlin/com/miguel/gastos/
│       └── data/util  # Driver/Factory/ConversorFechas
└── desktop-javafx/     # UI JavaFX
    └── src/main/
        ├── java/com/miguel/gastos/
        │   ├── MainApp.java
        │   ├── controller/
        │   └── service/ReportePdfService.java
        └── resources/
            ├── fxml/
            ├── css/
            └── reports/
```

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
./gradlew :desktop-javafx:run
```

## Notas de la migración Fase 2 (Paso 1)

- El dominio se migró a Kotlin `sealed class` (mutable por ahora) para mantener interoperabilidad con Java.
- Persistencia: SQLDelight genera el acceso a SQLite. El archivo BD existente en `~/.control-gastos/` se conserva.
- JasperReports se mantiene solo en `desktop-javafx`. La lógica de cálculo pasa a `:shared` (`ReporteCalculator`).
- API del repositorio es síncrona; se pasará a `suspend` cuando se añadan Android/iOS.
