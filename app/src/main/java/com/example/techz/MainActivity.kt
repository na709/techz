package com.example.techz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.techz.service.UserSession
import com.example.techz.ui.navigation.AppNavGraph
import com.example.techz.ui.navigation.Screen
import com.example.techz.ui.screens.cart.CartManager
//
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSession.initSession(this)
        CartManager.loadCart(this)
        setContent {
            val navController = rememberNavController()

            // - Nếu là Admin -> Vào thẳng trang quản lý (AdminHome)
            // - Nếu là Khách HOẶC User thường -> Vào trang Mua hàng (Home)
            val startScreen = if (UserSession.isLoggedIn && UserSession.isAdmin) {
                Screen.AdminDashboard.route
            } else {
                Screen.Home.route
            }

            // 3. Truyền NavController và màn hình bắt đầu vào AppNavGraph
            AppNavGraph(
                navController = navController,
                startDestination = startScreen
            )
        }
    }
}