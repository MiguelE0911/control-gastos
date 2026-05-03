# Control de Gastos Personales

Aplicación de escritorio para el control de gastos personales, desarrollada en Java como proyecto de aprendizaje progresivo.

## Tecnologías

- **Java 17**
- **JavaFX 21** — Interfaz gráfica
- **PostgreSQL** — Base de datos
- **Maven** — Gestión de dependencias
- **JasperReports** — Generación de reportes


## Arquitectura

El proyecto sigue una arquitectura en capas:
```
src/main/java/com/miguel/gastos/
│
├── model/        # Clases de dominio (POO + Herencia)
├── dao/          # Acceso a base de datos (Patrón DAO)
├── service/      # Lógica de negocio
├── controller/   # Controladores JavaFX
└── util/         # Utilidades y conexión a BD
```


## Licencia

Proyecto personal de uso educativo. Sin licencia definida.
