package com.miguel.gastos.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miguel.gastos.android.ui.Fechas
import com.miguel.gastos.data.GastoRepository
import com.miguel.gastos.model.Gasto
import com.miguel.gastos.service.GastoTipo
import com.miguel.gastos.service.ReporteCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Filtros de período predefinidos (reutilizan la lógica de `ReporteCalculator`). */
enum class PeriodoFiltro(val etiqueta: String) {
    TODO("Todo"),
    ESTE_MES("Este mes"),
    MES_ANTERIOR("Mes anterior"),
    ESTE_ANIO("Este año"),
}

data class GastosUiState(
    val cargando: Boolean = true,
    val tipo: GastoTipo? = null,
    val periodo: PeriodoFiltro = PeriodoFiltro.TODO,
    val busqueda: String = "",
    val gastosFiltrados: List<Gasto> = emptyList(),
    val totalFiltrado: Double = 0.0,
    val totalGeneral: Double = 0.0,
    val totalFijo: Double = 0.0,
    val totalVariable: Double = 0.0,
    val totalOcio: Double = 0.0,
) {
    val hayFiltros: Boolean
        get() = tipo != null || periodo != PeriodoFiltro.TODO || busqueda.isNotBlank()
}

/**
 * ViewModel de la pantalla de gastos.
 *
 * Envuelve la API **síncrona** del `GastoRepository` compartido con corrutinas:
 * las operaciones de disco se ejecutan en [Dispatchers.IO] y el resultado se
 * expone como un único [StateFlow] inmutable.
 */
class GastosViewModel(private val repository: GastoRepository) : ViewModel() {

    private val reporte = ReporteCalculator()
    private var todos: List<Gasto> = emptyList()

    private val _state = MutableStateFlow(GastosUiState())
    val state: StateFlow<GastosUiState> = _state.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true) }
            val lista = withContext(Dispatchers.IO) { repository.listarTodos() }
            todos = lista
            recalcular(cargando = false)
        }
    }

    fun guardar(gasto: Gasto, esNuevo: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (esNuevo) repository.insertar(gasto) else repository.actualizar(gasto)
            }
            recargar()
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.eliminar(id) }
            recargar()
        }
    }

    fun setTipo(tipo: GastoTipo?) {
        _state.update { it.copy(tipo = tipo) }
        recalcular()
    }

    fun setPeriodo(periodo: PeriodoFiltro) {
        _state.update { it.copy(periodo = periodo) }
        recalcular()
    }

    fun setBusqueda(texto: String) {
        _state.update { it.copy(busqueda = texto) }
        recalcular()
    }

    fun limpiarFiltros() {
        _state.update { it.copy(tipo = null, periodo = PeriodoFiltro.TODO, busqueda = "") }
        recalcular()
    }

    private fun recalcular(cargando: Boolean = _state.value.cargando) {
        val filtros = _state.value

        var lista = todos
        Fechas.rango(filtros.periodo)?.let { (desde, hasta) ->
            lista = reporte.filtrarPorPeriodo(lista, desde, hasta)
        }
        filtros.tipo?.let { tipo -> lista = lista.filter { it.tipoGasto == tipo.name } }
        if (filtros.busqueda.isNotBlank()) {
            lista = lista.filter { it.descripcion.contains(filtros.busqueda, ignoreCase = true) }
        }

        _state.update {
            it.copy(
                cargando = cargando,
                gastosFiltrados = lista,
                totalFiltrado = lista.sumOf { g -> g.monto },
                totalGeneral = reporte.totalGeneral(todos),
                totalFijo = reporte.totalPorTipo(todos, GastoTipo.FIJO),
                totalVariable = reporte.totalPorTipo(todos, GastoTipo.VARIABLE),
                totalOcio = reporte.totalPorTipo(todos, GastoTipo.OCIO),
            )
        }
    }

    companion object {
        fun factory(repository: GastoRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { GastosViewModel(repository) }
        }
    }
}
