package com.miguel.gastos.model

import kotlinx.datetime.LocalDate

class GastoOcio @JvmOverloads constructor(
    descripcion: String,
    monto: Double,
    fecha: LocalDate,
    var esRecurrente: Boolean,
    id: Int = 0,
) : Gasto(id, descripcion, monto, fecha) {
    override val tipoGasto: String get() = "OCIO"
}
