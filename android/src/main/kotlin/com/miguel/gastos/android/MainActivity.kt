package com.miguel.gastos.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miguel.gastos.android.ui.GastosApp
import com.miguel.gastos.android.ui.theme.GastosTheme
import com.miguel.gastos.android.viewmodel.GastosViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as GastosApplication).repository

        setContent {
            GastosTheme {
                val viewModel: GastosViewModel = viewModel(
                    factory = GastosViewModel.factory(repository),
                )
                GastosApp(viewModel = viewModel)
            }
        }
    }
}
