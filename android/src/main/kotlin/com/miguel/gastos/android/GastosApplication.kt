package com.miguel.gastos.android

import android.app.Application
import com.miguel.gastos.data.AndroidDriverFactory
import com.miguel.gastos.data.GastoRepository

/**
 * Punto de acceso a la capa compartida en Android.
 *
 * Crea un único [GastoRepository] (y por tanto un único driver SQLite) para toda
 * la app. La base de datos se ubica en el almacenamiento interno de la app
 * (`getDatabasePath`), gestionado por [AndroidDriverFactory].
 */
class GastosApplication : Application() {

    val repository: GastoRepository by lazy {
        GastoRepository(AndroidDriverFactory.createDriver(this))
    }
}
