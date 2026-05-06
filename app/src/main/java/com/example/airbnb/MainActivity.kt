package com.example.airbnb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.airbnb.navigation.AirbnbApp
import com.example.airbnb.ui.theme.AirbnbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AirbnbTheme {
                AirbnbApp()
            }
        }
    }
}
