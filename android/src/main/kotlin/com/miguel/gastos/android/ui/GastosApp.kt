package com.miguel.gastos.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miguel.gastos.android.viewmodel.GastosViewModel
import com.miguel.gastos.model.Gasto

/** Destino del formulario dentro de la app. */
sealed interface FormTarget {
    data object Cerrado : FormTarget
    data object Nuevo : FormTarget
    data class Editar(val gasto: Gasto) : FormTarget
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosApp(viewModel: GastosViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    var form by remember { mutableStateOf<FormTarget>(FormTarget.Cerrado) }
    var aEliminar by remember { mutableStateOf<Gasto?>(null) }

    val mostrandoForm = form != FormTarget.Cerrado
    val titulo = when {
        form is FormTarget.Editar -> "Editar gasto"
        form is FormTarget.Nuevo -> "Nuevo gasto"
        tab == 0 -> "Gastos"
        else -> "Resumen"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (mostrandoForm) {
                        IconButton(onClick = { form = FormTarget.Cerrado }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!mostrandoForm) {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("Gastos") },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                        label = { Text("Resumen") },
                    )
                }
            }
        },
        floatingActionButton = {
            if (!mostrandoForm && tab == 0) {
                FloatingActionButton(onClick = { form = FormTarget.Nuevo }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar gasto")
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                mostrandoForm -> FormularioGastoContent(
                    gasto = (form as? FormTarget.Editar)?.gasto,
                    onGuardar = { gasto, esNuevo ->
                        viewModel.guardar(gasto, esNuevo)
                        form = FormTarget.Cerrado
                    },
                    onCancelar = { form = FormTarget.Cerrado },
                )

                tab == 0 -> ListaGastosContent(
                    state = state,
                    onEditar = { form = FormTarget.Editar(it) },
                    onEliminar = { aEliminar = it },
                    onTipo = viewModel::setTipo,
                    onPeriodo = viewModel::setPeriodo,
                    onBusqueda = viewModel::setBusqueda,
                    onLimpiarFiltros = viewModel::limpiarFiltros,
                )

                else -> ResumenContent(state = state)
            }
        }
    }

    aEliminar?.let { gasto ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar gasto") },
            text = { Text("¿Eliminar \"${gasto.descripcion}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(gasto.id)
                    aEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}
