package com.example.techz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.techz.ui.screens.admin.AdminDashboardScreen
import com.example.techz.ui.screens.admin.AdminOrderScreen
import com.example.techz.ui.screens.cart.CartScreen
import com.example.techz.ui.screens.home.HomeScreen
import com.example.techz.ui.screens.login.LoginScreen
import com.example.techz.ui.screens.payment.PaymentScreen
import com.example.techz.ui.screens.product.ProductDetailScreen
import com.example.techz.ui.screens.product.ProductListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {

                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onClickRegister = {
                    // Xử lý khi bấm nút Đăng ký (nếu chưa có thì để trống hoặc Toast thông báo)
                    // Ví dụ: navController.navigate("register")
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onProductClick = { productId ->
                    navController.navigate("detail/$productId")
                },
                onCategoryClick = { category ->
                    navController.navigate("${Screen.ProductList.route}?category=$category")
                                  },
                onGoToCart = { navController.navigate(Screen.Cart.route) },
                onViewAll = { navController.navigate(Screen.ProductList.route) }
            )
        }

        composable(Screen.ProductList.route) {
            ProductListScreen(
                navController = navController,
                onProductClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onCheckout = { navController.navigate(Screen.Payment.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("id") ?: 0

            ProductDetailScreen(
                productId = productId,
                onAddToCart = { navController.navigate(Screen.Cart.route) },
                onBack = { navController.popBackStack() },
                onProductClick = { id -> navController.navigate("detail/$id") }
            )
        }

        composable(Screen.Payment.route) {
            PaymentScreen(
                onConfirm = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                onLogout = {
                    // Xử lý đăng xuất: Quay về Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Đã sửa lỗi ở đây: Xóa dấu } thừa, đưa AdminOrder vào cùng hàng với các composable khác
        composable(Screen.AdminOrder.route) {
            AdminOrderScreen(navController = navController)
        }

    } // Kết thúc NavHost
} // Kết thúc hàm AppNavGraph