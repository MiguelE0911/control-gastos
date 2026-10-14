package com.miguel.gastos.model

import kotlinx.datetime.LocalDate

class GastoVariable @JvmOverloads constructor(
    descripcion: String,
    monto: Double,
    fecha: LocalDate,
    var subcategoria: String,
    id: Int = 0,
) : Gasto(id, descripcion, monto, fecha) {
    override val tipoGasto: String get() = "VARIABLE"
}
