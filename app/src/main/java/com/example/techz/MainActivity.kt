package com.example.techz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.techz.service.UserSession
import com.example.techz.ui.navigation.AppNavGraph
import com.example.techz.ui.navigation.Screen
import com.example.techz.service.CartManager
import com.example.techz.service.RetrofitClient

//
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSession.initSession(applicationContext)
        RetrofitClient.init(applicationContext)
        CartManager.loadCart(this)
        android.util.Log.d("DEBUG_SESSION", "Role: ${UserSession.currentUserRole}, IsAdmin: ${UserSession.isAdmin}")
        setContent {
            val navController = rememberNavController()
            val startScreen = if (UserSession.isLoggedIn && UserSession.isAdmin) {
                Screen.AdminDashboard.route
            } else {
                Screen.Home.route
            }
            AppNavGraph(
                navController = navController,
                startDestination = startScreen
            )
        }
    }
}