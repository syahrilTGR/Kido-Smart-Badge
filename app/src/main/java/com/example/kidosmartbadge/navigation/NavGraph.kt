package com.example.kidosmartbadge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kidosmartbadge.ui.addchild.AddChildScreen
import com.example.kidosmartbadge.ui.home.HomeScreen
import com.example.kidosmartbadge.ui.linkcard.LinkCardScreen
import com.example.kidosmartbadge.ui.login.LoginScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable(
            route = "home/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "Orang Tua"
            HomeScreen(navController = navController, role = role)
        }
        composable("add_child") {
            AddChildScreen(navController = navController)
        }
        composable(
            route = "link_card/{childName}",
            arguments = listOf(navArgument("childName") { type = NavType.StringType })
        ) { backStackEntry ->
            val childName = backStackEntry.arguments?.getString("childName") ?: ""
            LinkCardScreen(navController = navController, childName = childName)
        }
    }
}
