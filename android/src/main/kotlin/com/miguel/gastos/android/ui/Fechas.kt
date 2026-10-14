package com.miguel.gastos.android.ui

import com.miguel.gastos.android.viewmodel.PeriodoFiltro
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.util.Calendar

/**
 * Utilidades de fecha para Android.
 *
 * Se basan en la API de plataforma (`java.util.Calendar`) para obtener "hoy" y el
 * último día del mes, evitando depender de `Clock`/`TimeZone` de kotlinx-datetime
 * (que en 0.8.0 vive en `kotlin.time`). El dominio sigue usando `LocalDate`.
 */
object Fechas {

    fun hoy(): LocalDate {
        val c = Calendar.getInstance()
        return LocalDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun formatear(fecha: LocalDate): String =
        "%02d/%02d/%04d".format(fecha.day, fecha.month.number, fecha.year)

    private fun ultimoDiaDelMes(anio: Int, mes: Int): Int {
        val c = Calendar.getInstance()
        c.set(anio, mes - 1, 1)
        return c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Rango [desde, hasta] (inclusive) para el periodo; `null` si es "Todo". */
    fun rango(periodo: PeriodoFiltro): Pair<LocalDate, LocalDate>? {
        val hoy = hoy()
        val anio = hoy.year
        val mes = hoy.month.number
        return when (periodo) {
            PeriodoFiltro.TODO -> null
            PeriodoFiltro.ESTE_MES ->
                LocalDate(anio, mes, 1) to LocalDate(anio, mes, ultimoDiaDelMes(anio, mes))
            PeriodoFiltro.MES_ANTERIOR -> {
                val (a, m) = if (mes == 1) (anio - 1) to 12 else anio to (mes - 1)
                LocalDate(a, m, 1) to LocalDate(a, m, ultimoDiaDelMes(a, m))
            }
            PeriodoFiltro.ESTE_ANIO -> LocalDate(anio, 1, 1) to LocalDate(anio, 12, 31)
        }
    }
}
