package com.example.techz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.techz.ui.screens.*
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
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onProductClick = { id -> navController.navigate(Screen.Detail.passId(id)) },
                onGoToCart = { navController.navigate(Screen.Cart.route) },
                onViewAll = { navController.navigate(Screen.ProductList.route) }
            )
        }
        composable(Screen.ProductList.route) {
            ProductListScreen(
                navController = navController,
                onProductClick = { id -> navController.navigate(Screen.Detail.passId(id)) }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(onCheckout = { navController.navigate(Screen.Payment.route) }, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { back ->
            ProductDetailScreen(
                productId = back.arguments?.getString("id"),
                onAddToCart = { navController.navigate(Screen.Cart.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Payment.route) {
            PaymentScreen(
                onConfirm = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }
    }
}