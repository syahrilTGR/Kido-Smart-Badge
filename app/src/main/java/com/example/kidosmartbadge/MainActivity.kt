package com.example.kidosmartbadge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.kidosmartbadge.navigation.NavGraph
import com.example.kidosmartbadge.ui.theme.KidoSmartBadgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KidoSmartBadgeTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}