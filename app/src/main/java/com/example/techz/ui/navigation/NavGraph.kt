package com.example.techz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.techz.service.UserSession
import com.example.techz.ui.screens.account.AccountChangePasswordScreen
import com.example.techz.ui.screens.account.AccountDetailScreen
import com.example.techz.ui.screens.account.AccountLoggedScreen
import com.example.techz.ui.screens.account.AccountOrderScreen
import com.example.techz.ui.screens.account.AccountScreen
import com.example.techz.ui.screens.admin.AdminDashboardScreen
import com.example.techz.ui.screens.admin.AdminOrderScreen
import com.example.techz.ui.screens.cart.CartScreen
import com.example.techz.ui.screens.home.HomeScreen
import com.example.techz.ui.screens.login.LoginScreen
import com.example.techz.ui.screens.payment.PaymentScreen
import com.example.techz.ui.screens.product.ProductDetailScreen
import com.example.techz.ui.screens.product.ProductListScreen
import com.example.techz.ui.screens.register.RegisterScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {


    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Login.route) {
            LoginScreen(

                onLoginSuccess = { role ->
                        if (role == "admin")
                        {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(0) { inclusive = true }}
                        }else{
                            navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        }
                    },
                onClickRegister =
                    {
                        navController.navigate(Screen.Register.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen (
                onRegister = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToLogin =
                    {
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Login.route) { inclusive = true } }
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

        composable(
            route = "${Screen.ProductList.route}?category={category}",
            arguments = listOf(
                navArgument("category") { nullable = true }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")

            ProductListScreen(
                navController = navController,
                initialCategory = category,
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

        composable(Screen.Account.route) {
            if (UserSession.isLoggedIn) {
                AccountLoggedScreen(
                    navController = navController,
                    onLogout = {
                        UserSession.logout(navController.context)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onGoToInfo = {
                        navController.navigate(Screen.AccountDetail.route) { popUpTo(Screen.AccountLogged.route) { inclusive = true } }
                    },
                    onGoToOrders = {
                        navController.navigate(Screen.OrderHistory.route) { popUpTo(Screen.AccountLogged.route) { inclusive = true } }
                    },
                    onGoToChangePass = {
                        navController.navigate(Screen.ChangePassword.route) { popUpTo(Screen.AccountLogged.route) { inclusive = true } }
                    }
                )
            } else {
                AccountScreen(
                    navController = navController,
                    onGoToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Account.route) {
                                inclusive = true
                            }
                        }
                    },
                    onLogout = {
                        UserSession.logout(navController.context)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onGoToRegister = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(
                                Screen.Account.route
                            ) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.OrderHistory.route) {
            AccountOrderScreen(navController = navController)
        }

        composable(Screen.ChangePassword.route) {
            AccountChangePasswordScreen(navController = navController)
        }
        composable(Screen.AccountDetail.route) {
            AccountDetailScreen(navController = navController)
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                onLogout = {
                    UserSession.logout(navController.context)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminOrder.route) {
            AdminOrderScreen(navController = navController)
        }

    }
}