# Control de Gastos Personales

Aplicación de escritorio para el control de gastos personales, desarrollada en Java como proyecto de aprendizaje progresivo.

## Tecnologías

- **Java 21**
- **JavaFX 21** — Interfaz gráfica
- **SQLite** — Base de datos local (archivo `~/.control-gastos/control_gastos.db`)
- **Maven** — Gestión de dependencias
- **JasperReports / OpenPDF** — Generación de reportes PDF

## Estructura del proyecto

Proyecto Maven multimódulo:

```
control-gastos/
├── pom.xml             # POM padre: versiones y configuración común
├── core-jvm/           # Núcleo de negocio (sin JavaFX): modelo, DAO, servicio
│   └── src/main/java/com/miguel/gastos/
│       ├── model/      # Clases de dominio (POO + Herencia)
│       ├── dao/        # Acceso a datos (Patrón DAO)
│       ├── service/    # Lógica de reportes
│       └── util/       # Conexión a la base de datos
└── desktop-javafx/     # Aplicación de escritorio
    └── src/main/
        ├── java/com/miguel/gastos/
        │   ├── MainApp.java        # Punto de entrada
        │   └── controller/         # Controladores JavaFX
        └── resources/
            ├── fxml/               # Vistas
            ├── css/                # Tema oscuro
            └── reports/            # Plantillas JasperReports
```

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
mvn clean install
mvn -pl desktop-javafx javafx:run
```

O entrando directamente al módulo de escritorio:

```
cd desktop-javafx
mvn javafx:run
```

> Nota: no ejecutes `javafx:run` desde la raíz; el proyecto raíz es de tipo `pom` y no contiene la clase principal.

## Licencia

Proyecto personal de uso educativo. Sin licencia definida.
