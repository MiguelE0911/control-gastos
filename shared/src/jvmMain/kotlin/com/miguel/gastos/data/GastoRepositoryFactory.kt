package com.miguel.gastos.data

/**
 * Punto único de acceso al repositorio desde Java.
 * Mantiene una instancia singleton (un único SqlDriver/connection).
 */
object GastoRepositoryFactory {

    private val delegate = lazy { GastoRepository(DriverFactory.createDriver()) }

    @JvmStatic
    fun get(): GastoRepository = delegate.value

    @JvmStatic
    fun close() {
        if (delegate.isInitialized()) {
            delegate.value.cerrar()
        }
        DriverFactory.closeDriver()
    }
}
