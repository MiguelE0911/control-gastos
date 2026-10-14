package com.miguel.gastos.service

import com.miguel.gastos.model.Gasto
import com.miguel.gastos.model.dto.ReporteGastoDTO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/** Tipos de gasto soportados. */
enum class GastoTipo { FIJO, VARIABLE, OCIO }

/**
 * Lógica pura de reportes: filtrado por período, totales y armado de filas (DTO).
 * No depende de plataforma ni de JasperReports.
 */
class ReporteCalculator {

    fun filtrarPorPeriodo(gastos: List<Gasto>, desde: LocalDate, hasta: LocalDate): List<Gasto> =
        gastos.filter { it.fecha in desde..hasta }

    fun totalGeneral(gastos: List<Gasto>): Double = gastos.sumOf { it.monto }

    fun totalPorTipo(gastos: List<Gasto>, tipo: GastoTipo): Double =
        gastos.filter { it.tipoGasto == tipo.name }.sumOf { it.monto }

    fun convertirADTO(gastos: List<Gasto>): List<ReporteGastoDTO> = gastos.map { g ->
        ReporteGastoDTO(
            tipo = g.tipoGasto,
            descripcion = g.descripcion,
            monto = g.monto,
            fecha = formatearFecha(g.fecha),
            detalle = g.detalle,
        )
    }

    /** Formato dd/MM/yyyy exigido por las plantillas .jrxml. */
    fun formatearFecha(fecha: LocalDate): String {
        val dia = fecha.day.toString().padStart(2, '0')
        val mes = fecha.month.number.toString().padStart(2, '0')
        return "$dia/$mes/${fecha.year}"
    }
}
