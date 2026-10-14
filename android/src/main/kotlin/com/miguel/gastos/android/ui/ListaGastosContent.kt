package com.miguel.gastos.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miguel.gastos.android.viewmodel.GastosUiState
import com.miguel.gastos.android.viewmodel.PeriodoFiltro
import com.miguel.gastos.model.Gasto
import com.miguel.gastos.service.GastoTipo

@Composable
fun ListaGastosContent(
    state: GastosUiState,
    onEditar: (Gasto) -> Unit,
    onEliminar: (Gasto) -> Unit,
    onTipo: (GastoTipo?) -> Unit,
    onPeriodo: (PeriodoFiltro) -> Unit,
    onBusqueda: (String) -> Unit,
    onLimpiarFiltros: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.busqueda,
            onValueChange = onBusqueda,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (state.busqueda.isNotEmpty()) {
                    IconButton(onClick = { onBusqueda("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            placeholder = { Text("Buscar por descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.tipo == null,
                onClick = { onTipo(null) },
                label = { Text("Todos") },
            )
            GastoTipo.entries.forEach { tipo ->
                FilterChip(
                    selected = state.tipo == tipo,
                    onClick = { onTipo(tipo) },
                    label = { Text(etiquetaTipo(tipo)) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PeriodoFiltro.entries.forEach { periodo ->
                FilterChip(
                    selected = state.periodo == periodo,
                    onClick = { onPeriodo(periodo) },
                    label = { Text(periodo.etiqueta) },
                )
            }
            if (state.hayFiltros) {
                TextButton(onClick = onLimpiarFiltros) { Text("Limpiar") }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total (filtrado)", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = formatearMonto(state.totalFiltrado),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("${state.gastosFiltrados.size} gasto(s)", style = MaterialTheme.typography.bodySmall)
            }
        }

        when {
            state.cargando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.gastosFiltrados.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (state.hayFiltros) "Sin resultados para los filtros" else "No hay gastos registrados")
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.gastosFiltrados, key = { it.id }) { gasto ->
                    ListItem(
                        headlineContent = { Text(gasto.descripcion) },
                        supportingContent = {
                            val detalle = gasto.detalle
                            Text(
                                listOfNotNull(
                                    etiquetaTipo(tipoDe(gasto)),
                                    Fechas.formatear(gasto.fecha),
                                    detalle.ifBlank { null },
                                ).joinToString(" · ")
                            )
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatearMonto(gasto.monto), fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { onEliminar(gasto) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                }
                            }
                        },
                        modifier = Modifier.clickable { onEditar(gasto) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
