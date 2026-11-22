package com.group04.materialexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.group04.materialexchange.navigation.MaterialExchangeApp
import com.group04.materialexchange.ui.theme.ExchangeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExchangeTheme {
                MaterialExchangeApp()
            }
        }
    }
}
