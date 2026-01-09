package com.example.techz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.techz.ui.screens.cart.CartScreen
import com.example.techz.ui.screens.home.HomeScreen
import com.example.techz.ui.screens.login.LoginScreen
import com.example.techz.ui.screens.payment.PaymentScreen
import com.example.techz.ui.screens.product.ProductDetailScreen
import com.example.techz.ui.screens.product.ProductListScreen
import com.example.techz.ui.screens.register.RegisterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess =
                    {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    },
                onClickRegister =
                    {
                        navController.navigate(Screen.Register.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }

            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onProductClick = { productId ->
                    navController.navigate("detail/$productId")
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
        composable(Screen.Register.route) {
            RegisterScreen(onRegister = {
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } }
            },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } }
                })
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