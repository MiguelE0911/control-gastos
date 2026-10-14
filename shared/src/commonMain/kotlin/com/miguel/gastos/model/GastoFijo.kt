package com.miguel.gastos.model

import kotlinx.datetime.LocalDate

class GastoFijo @JvmOverloads constructor(
    descripcion: String,
    monto: Double,
    fecha: LocalDate,
    var diaPago: Int,
    id: Int = 0,
) : Gasto(id, descripcion, monto, fecha) {
    override val tipoGasto: String get() = "FIJO"
}
