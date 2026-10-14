package com.miguel.gastos.model

import kotlinx.datetime.LocalDate

/**
 * Raíz de la jerarquía de gastos.
 *
 * Se mantiene mutable (var) de forma deliberada en este paso para conservar la
 * interoperabilidad con el código Java del escritorio. En una fase posterior se
 * puede migrar a `data class` inmutable.
 */
sealed class Gasto(
    var id: Int,
    var descripcion: String,
    var monto: Double,
    var fecha: LocalDate,
) {
    /** Etiqueta usada por la persistencia y los reportes. */
    abstract val tipoGasto: String

    /** Texto del atributo específico de cada subtipo (columna "detalle"). */
    val detalle: String
        get() = when (this) {
            is GastoFijo -> "Día de pago: $diaPago"
            is GastoVariable -> subcategoria
            is GastoOcio -> if (esRecurrente) "Recurrente" else "Única vez"
        }

    override fun toString(): String =
        "[$tipoGasto] $descripcion - Q$monto ($fecha)"
}
