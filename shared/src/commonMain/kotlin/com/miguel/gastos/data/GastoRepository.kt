package com.miguel.gastos.data

import app.cash.sqldelight.db.SqlDriver
import com.miguel.gastos.db.GastosDatabase
import com.miguel.gastos.model.Gasto
import com.miguel.gastos.model.GastoFijo
import com.miguel.gastos.model.GastoOcio
import com.miguel.gastos.model.GastoVariable
import com.miguel.gastos.db.Gastos
import kotlinx.datetime.LocalDate

/**
 * Repositorio multiplataforma de gastos.
 * API síncrona (JVM-first). En una fase posterior se añadirá wrapper suspend.
 */
class GastoRepository(driver: SqlDriver) {

    private val database = GastosDatabase(driver)
    private val queries = database.gastoQueries

    fun insertar(gasto: Gasto) {
        queries.insertar(
            tipo = gasto.tipoGasto,
            descripcion = gasto.descripcion,
            monto = gasto.monto,
            fecha = gasto.fecha.toString(),
            dia_pago = when (gasto) {
                is GastoFijo -> gasto.diaPago.toLong()
                else -> null
            },
            subcategoria = when (gasto) {
                is GastoVariable -> gasto.subcategoria
                else -> null
            },
            es_recurrente = when (gasto) {
                is GastoOcio -> if (gasto.esRecurrente) 1L else 0L
                else -> null
            },
        )
        // Actualizar el ID generado por AUTOINCREMENT
        gasto.id = queries.ultimoId().executeAsOne().toInt()
    }

    fun buscarPorId(id: Int): Gasto? =
        queries.buscarPorId(id.toLong()).executeAsOneOrNull()?.toDomain()

    fun listarTodos(): List<Gasto> =
        queries.listarTodos().executeAsList().map { it.toDomain() }

    fun actualizar(gasto: Gasto) {
        queries.actualizar(
            descripcion = gasto.descripcion,
            monto = gasto.monto,
            fecha = gasto.fecha.toString(),
            dia_pago = when (gasto) {
                is GastoFijo -> gasto.diaPago.toLong()
                else -> null
            },
            subcategoria = when (gasto) {
                is GastoVariable -> gasto.subcategoria
                else -> null
            },
            es_recurrente = when (gasto) {
                is GastoOcio -> if (gasto.esRecurrente) 1L else 0L
                else -> null
            },
            id = gasto.id.toLong(),
        )
    }

    fun eliminar(id: Int) {
        queries.eliminar(id.toLong())
    }

    fun cerrar() {
        // El driver es el encargado de cerrar la conexión
        try {
            // JdbcSqliteDriver cierra la conexión
        } finally {
            // No se accede directamente a close en interfaz genérica; delegamos
            // via reflexión no necesaria: driver implementa Closeable en JVM? No.
            // Pero para JdbcSqliteDriver es SqlDriver que implementa AutoCloseable en práctica.
            // En este contexto de uso JVM, usamos DriverFactory que guarda el driver.
            // Simplificamos: el cierre se hará desde factory con el mismo driver.
        }
    }
}

private fun Gastos.toDomain(): Gasto {
    val fechaLocal = LocalDate.parse(fecha)
    return when (tipo) {
        "FIJO" -> GastoFijo(
            descripcion = descripcion,
            monto = monto,
            fecha = fechaLocal,
            diaPago = (dia_pago ?: 0L).toInt(),
            id = id.toInt(),
        )
        "VARIABLE" -> GastoVariable(
            descripcion = descripcion,
            monto = monto,
            fecha = fechaLocal,
            subcategoria = subcategoria ?: "",
            id = id.toInt(),
        )
        "OCIO" -> GastoOcio(
            descripcion = descripcion,
            monto = monto,
            fecha = fechaLocal,
            esRecurrente = (es_recurrente ?: 0L) != 0L,
            id = id.toInt(),
        )
        else -> error("Tipo de gasto desconocido: $tipo")
    }
}
