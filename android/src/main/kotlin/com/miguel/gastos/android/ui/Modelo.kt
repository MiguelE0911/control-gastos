package com.miguel.gastos.android.ui

import com.miguel.gastos.model.Gasto
import com.miguel.gastos.model.GastoFijo
import com.miguel.gastos.model.GastoOcio
import com.miguel.gastos.model.GastoVariable
import com.miguel.gastos.service.GastoTipo
import java.util.Locale

/** Deriva el [GastoTipo] de una instancia concreta del dominio. */
fun tipoDe(gasto: Gasto): GastoTipo = when (gasto) {
    is GastoFijo -> GastoTipo.FIJO
    is GastoVariable -> GastoTipo.VARIABLE
    is GastoOcio -> GastoTipo.OCIO
}

/** Etiqueta legible para el tipo de gasto. */
fun etiquetaTipo(tipo: GastoTipo): String = when (tipo) {
    GastoTipo.FIJO -> "Fijo"
    GastoTipo.VARIABLE -> "Variable"
    GastoTipo.OCIO -> "Ocio"
}

/** Formatea un monto con dos decimales (punto decimal, estable en cualquier locale). */
fun formatearMonto(monto: Double): String = "\$" + String.format(Locale.US, "%.2f", monto)
