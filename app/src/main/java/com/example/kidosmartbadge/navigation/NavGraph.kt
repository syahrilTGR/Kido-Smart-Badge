package com.example.kidosmartbadge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.kidosmartbadge.ui.addchild.AddChildScreen
import com.example.kidosmartbadge.ui.childdetail.ChildDetailScreen
import com.example.kidosmartbadge.ui.home.HomeScreen
import com.example.kidosmartbadge.ui.linkcard.LinkCardScreen
import com.example.kidosmartbadge.ui.login.LoginScreen
import com.example.kidosmartbadge.ui.register.RegisterScreen
import com.example.kidosmartbadge.ui.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        navigation(
            startDestination = "home/{role}",
            route = "home_root"
        ) {
            composable(
                route = "home/{role}",
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "Ortu"
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
            composable(
                route = "child_detail/{childName}/{childRfidUid}",
                arguments = listOf(
                    navArgument("childName") { type = NavType.StringType },
                    navArgument("childRfidUid") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val childName = backStackEntry.arguments?.getString("childName") ?: ""
                val childRfidUid = backStackEntry.arguments?.getString("childRfidUid") ?: ""
                ChildDetailScreen(childName = childName, childRfidUid = childRfidUid)
            }
        }
    }
}
