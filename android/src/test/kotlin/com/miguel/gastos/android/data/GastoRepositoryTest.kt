package com.miguel.gastos.android.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.miguel.gastos.data.GastoRepository
import com.miguel.gastos.db.GastosDatabase
import com.miguel.gastos.model.GastoFijo
import com.miguel.gastos.model.GastoOcio
import com.miguel.gastos.model.GastoVariable
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas básicas de CRUD contra la capa de datos compartida, usando un driver
 * SQLite en memoria (se ejecutan en la JVM vía `testDebugUnitTest`, sin dispositivo).
 */
class GastoRepositoryTest {

    private lateinit var repo: GastoRepository
    private val fecha = LocalDate(2026, 10, 14)

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        GastosDatabase.Schema.create(driver)
        repo = GastoRepository(driver)
    }

    @Test
    fun insertarAsignaIdYListaLosGastos() {
        repo.insertar(GastoVariable("Supermercado", 120.5, fecha, "Alimentos"))
        repo.insertar(GastoFijo("Alquiler", 800.0, fecha, 5))

        val lista = repo.listarTodos()
        assertEquals(2, lista.size)
        assertTrue(lista.all { it.id > 0 })
    }

    @Test
    fun buscarPorIdDevuelveElGastoCorrecto() {
        repo.insertar(GastoFijo("Internet", 50.0, fecha, 10))
        val id = repo.listarTodos().first().id

        val encontrado = repo.buscarPorId(id)
        assertNotNull(encontrado)
        assertEquals("Internet", encontrado!!.descripcion)
        assertEquals("FIJO", encontrado.tipoGasto)
    }

    @Test
    fun actualizarModificaLosCampos() {
        repo.insertar(GastoOcio("Cine", 15.0, fecha, false))
        val original = repo.listarTodos().first()

        repo.actualizar(GastoOcio("Cine + cena", 45.0, fecha, true, original.id))

        val actual = repo.buscarPorId(original.id) as GastoOcio
        assertEquals("Cine + cena", actual.descripcion)
        assertEquals(45.0, actual.monto, 0.001)
        assertTrue(actual.esRecurrente)
    }

    @Test
    fun eliminarQuitaElGasto() {
        repo.insertar(GastoVariable("Taxi", 8.0, fecha, "Transporte"))
        val id = repo.listarTodos().first().id

        repo.eliminar(id)

        assertNull(repo.buscarPorId(id))
        assertTrue(repo.listarTodos().isEmpty())
    }
}
