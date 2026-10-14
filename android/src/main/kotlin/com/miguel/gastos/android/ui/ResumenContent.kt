package com.miguel.gastos.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miguel.gastos.android.viewmodel.GastosUiState

@Composable
fun ResumenContent(state: GastosUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TarjetaTotal("Total general", state.totalGeneral, destacado = true)
        TarjetaTotal("Fijos", state.totalFijo)
        TarjetaTotal("Variables", state.totalVariable)
        TarjetaTotal("Ocio", state.totalOcio)
    }
}

@Composable
private fun TarjetaTotal(titulo: String, monto: Double, destacado: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text(
                text = formatearMonto(monto),
                style = if (destacado) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
