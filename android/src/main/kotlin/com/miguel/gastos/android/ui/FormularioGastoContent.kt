package com.miguel.gastos.android.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.miguel.gastos.model.Gasto
import com.miguel.gastos.model.GastoFijo
import com.miguel.gastos.model.GastoOcio
import com.miguel.gastos.model.GastoVariable
import com.miguel.gastos.service.GastoTipo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * Formulario de alta/edición de un gasto.
 *
 * @param gasto `null` para crear uno nuevo; una instancia existente para editar.
 * @param onGuardar recibe el gasto construido y si es nuevo (para insertar vs actualizar).
 */
@Composable
fun FormularioGastoContent(
    gasto: Gasto?,
    onGuardar: (Gasto, Boolean) -> Unit,
    onCancelar: () -> Unit,
) {
    val context = LocalContext.current
    val esNuevo = gasto == null

    var tipo by remember { mutableStateOf(gasto?.let(::tipoDe) ?: GastoTipo.VARIABLE) }
    var descripcion by remember { mutableStateOf(gasto?.descripcion.orEmpty()) }
    var montoTexto by remember { mutableStateOf(gasto?.monto?.toString().orEmpty()) }
    var fecha by remember { mutableStateOf(gasto?.fecha ?: Fechas.hoy()) }
    var diaPagoTexto by remember { mutableStateOf((gasto as? GastoFijo)?.diaPago?.toString().orEmpty()) }
    var subcategoria by remember { mutableStateOf((gasto as? GastoVariable)?.subcategoria.orEmpty()) }
    var esRecurrente by remember { mutableStateOf((gasto as? GastoOcio)?.esRecurrente ?: false) }

    var errorDescripcion by remember { mutableStateOf<String?>(null) }
    var errorMonto by remember { mutableStateOf<String?>(null) }
    var errorDiaPago by remember { mutableStateOf<String?>(null) }
    var errorSubcategoria by remember { mutableStateOf<String?>(null) }

    fun guardar() {
        val monto = montoTexto.trim().replace(',', '.').toDoubleOrNull()
        val diaPago = diaPagoTexto.trim().toIntOrNull()

        errorDescripcion = if (descripcion.isBlank()) "Ingresa una descripción" else null
        errorMonto = if (monto == null || monto <= 0.0) "Ingresa un monto válido" else null
        errorDiaPago =
            if (tipo == GastoTipo.FIJO && (diaPago == null || diaPago !in 1..31)) "Día entre 1 y 31" else null
        errorSubcategoria =
            if (tipo == GastoTipo.VARIABLE && subcategoria.isBlank()) "Ingresa una subcategoría" else null

        if (errorDescripcion != null || errorMonto != null || errorDiaPago != null || errorSubcategoria != null) {
            return
        }

        val id = gasto?.id ?: 0
        val resultado: Gasto = when (tipo) {
            GastoTipo.FIJO -> GastoFijo(descripcion.trim(), monto!!, fecha, diaPago!!, id)
            GastoTipo.VARIABLE -> GastoVariable(descripcion.trim(), monto!!, fecha, subcategoria.trim(), id)
            GastoTipo.OCIO -> GastoOcio(descripcion.trim(), monto!!, fecha, esRecurrente, id)
        }
        onGuardar(resultado, esNuevo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tipo de gasto", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GastoTipo.entries.forEach { t ->
                FilterChip(
                    selected = tipo == t,
                    onClick = { tipo = t },
                    label = { Text(etiquetaTipo(t)) },
                )
            }
        }

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it; errorDescripcion = null },
            label = { Text("Descripción") },
            isError = errorDescripcion != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        errorDescripcion?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(
            value = montoTexto,
            onValueChange = { montoTexto = it; errorMonto = null },
            label = { Text("Monto") },
            isError = errorMonto != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        errorMonto?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedButton(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, anio, mes, dia -> fecha = LocalDate(anio, mes + 1, dia) },
                    fecha.year,
                    fecha.month.number - 1,
                    fecha.day,
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Fecha: ${Fechas.formatear(fecha)}")
        }

        when (tipo) {
            GastoTipo.FIJO -> {
                OutlinedTextField(
                    value = diaPagoTexto,
                    onValueChange = { diaPagoTexto = it; errorDiaPago = null },
                    label = { Text("Día de pago (1-31)") },
                    isError = errorDiaPago != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                errorDiaPago?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            GastoTipo.VARIABLE -> {
                OutlinedTextField(
                    value = subcategoria,
                    onValueChange = { subcategoria = it; errorSubcategoria = null },
                    label = { Text("Subcategoría") },
                    isError = errorSubcategoria != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorSubcategoria?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            GastoTipo.OCIO -> Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = esRecurrente, onCheckedChange = { esRecurrente = it })
                Spacer(Modifier.width(8.dp))
                Text("¿Es recurrente?")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(onClick = { guardar() }, modifier = Modifier.weight(1f)) {
                Text(if (esNuevo) "Agregar" else "Guardar")
            }
        }
    }
}
