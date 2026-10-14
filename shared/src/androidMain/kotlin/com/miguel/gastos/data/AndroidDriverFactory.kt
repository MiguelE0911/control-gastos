package com.miguel.gastos.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.miguel.gastos.db.GastosDatabase

/**
 * Factory del driver de SQLite para Android.
 *
 * A diferencia de JVM, `AndroidSqliteDriver` recibe el `Schema` y crea/actualiza
 * la base de datos automáticamente la primera vez que se abre.
 */
object AndroidDriverFactory {

    private const val DB_NAME = "control_gastos.db"

    fun createDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(
            schema = GastosDatabase.Schema,
            context = context.applicationContext,
            name = DB_NAME,
        )
}
