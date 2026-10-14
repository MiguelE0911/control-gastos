package com.miguel.gastos.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.miguel.gastos.db.GastosDatabase
import java.io.File
import java.sql.DriverManager
import java.time.Instant
import java.time.ZoneId

/**
 * Factory para el driver de SQLite (JVM).
 * Mantiene compatibilidad con el archivo DB existente en ~/.control-gastos/control_gastos.db.
 */
object DriverFactory {

    private const val DB_NAME = "control_gastos.db"
    private const val TABLA_GASTOS = "gastos"

    /** Formato esperado por el dominio compartido: ISO-8601 (YYYY-MM-DD). */
    private val FORMATO_FECHA_ISO = Regex("""\d{4}-\d{2}-\d{2}""")

    private var driverActual: SqlDriver? = null

    fun createDriver(): SqlDriver {
        driverActual?.let { return it }
        val archivo = rutaBaseDatos()
        val url = "jdbc:sqlite:${archivo.absolutePath}"
        val driver = JdbcSqliteDriver(url)
        if (!tablaExiste(archivo)) {
            GastosDatabase.Schema.create(driver)
        }
        migrarFechasLegadas(archivo)
        driverActual = driver
        return driver
    }

    fun closeDriver() {
        driverActual?.let { drv ->
            try {
                // JdbcSqliteDriver implementa AutoCloseable/closeable.
                val closeable = drv as? java.lang.AutoCloseable
                closeable?.close()
            } catch (_: Exception) {
                // no-op
            }
        }
        driverActual = null
    }

    private fun rutaBaseDatos(): File {
        val dir = File(System.getProperty("user.home"), ".control-gastos")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, DB_NAME)
    }

    private fun tablaExiste(archivo: File): Boolean {
        DriverManager.getConnection("jdbc:sqlite:${archivo.absolutePath}").use { conexion ->
            conexion.createStatement().use { stmt ->
                stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLA_GASTOS'"
                ).use { rs ->
                    return rs.next()
                }
            }
        }
    }

    /**
     * Migra registros creados por la versión Java anterior, que almacenaba la fecha
     * como milisegundos epoch (p. ej. "1785646800000") en lugar de ISO-8601.
     *
     * Solo afecta a filas cuyo valor de `fecha` no cumple el patrón ISO, por lo que
     * es idempotente y seguro de ejecutar en cada arranque.
     */
    private fun migrarFechasLegadas(archivo: File) {
        DriverManager.getConnection("jdbc:sqlite:${archivo.absolutePath}").use { conexion ->
            val filasLegadas = mutableListOf<Pair<Long, String>>()
            conexion.createStatement().use { stmt ->
                stmt.executeQuery("SELECT id, fecha FROM $TABLA_GASTOS").use { rs ->
                    while (rs.next()) {
                        val fecha = rs.getString("fecha") ?: continue
                        if (!FORMATO_FECHA_ISO.matches(fecha)) {
                            filasLegadas.add(rs.getLong("id") to fecha)
                        }
                    }
                }
            }
            if (filasLegadas.isEmpty()) return

            conexion.prepareStatement("UPDATE $TABLA_GASTOS SET fecha = ? WHERE id = ?").use { ps ->
                for ((id, fecha) in filasLegadas) {
                    val millis = fecha.toLongOrNull() ?: continue
                    val iso = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                    ps.setString(1, iso)
                    ps.setLong(2, id)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
            println("Migradas ${filasLegadas.size} fecha(s) legada(s) (epoch-ms -> ISO-8601).")
        }
    }
}
