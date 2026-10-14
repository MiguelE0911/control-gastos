package com.miguel.gastos.model.dto

/**
 * Fila usada como fuente de datos de los reportes JasperReports.
 * El formato de `fecha` ya viene como "dd/MM/yyyy" (requisito de las plantillas).
 */
data class ReporteGastoDTO(
    val tipo: String,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val detalle: String,
)
