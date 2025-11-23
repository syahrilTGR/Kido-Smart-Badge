package com.example.kidosmartbadge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.kidosmartbadge.navigation.NavGraph
import com.example.kidosmartbadge.ui.theme.KidoSmartBadgeTheme

class MainActivity : ComponentActivity() {

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseViewModel.initialize()
        setContent {
            KidoSmartBadgeTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KidoSmartBadgeTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}