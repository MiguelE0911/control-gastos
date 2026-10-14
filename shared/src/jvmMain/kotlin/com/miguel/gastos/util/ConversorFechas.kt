package com.miguel.gastos.util

import kotlinx.datetime.LocalDate as KotlinLocalDate
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.number

/**
 * Conversión de fechas entre java.time.LocalDate y kotlinx.datetime.LocalDate.
 * Usado por el código Java de escritorio (no disponible en commonMain).
 */
object ConversorFechas {
    @JvmStatic
    fun aKotlin(fecha: JavaLocalDate): KotlinLocalDate =
        KotlinLocalDate(fecha.year, fecha.monthValue, fecha.dayOfMonth)

    @JvmStatic
    fun aJava(fecha: KotlinLocalDate): JavaLocalDate =
        JavaLocalDate.of(fecha.year, fecha.month.number, fecha.day)
}
