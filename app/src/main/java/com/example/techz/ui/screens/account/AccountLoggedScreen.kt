package com.example.techz.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.techz.service.UserSession
import com.example.techz.ui.components.TechZBottomBar

@Composable
fun AccountLoggedScreen(
    navController: NavHostController,
    onLogout:() -> Unit,
    onGoToInfo: () -> Unit,
    onGoToOrders:() -> Unit,
    onGoToChangePass: () -> Unit
) {
    val brandColor = Color(0xFF00A9FF)
    val userName = UserSession.currentUserName ?: "Bạn"
    val avatarUrl = "https://s3.cloudfly.vn/techz-avatar/avatar.jpg"

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brandColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TechZ Store", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = { TechZBottomBar(navController, userName) },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl).crossfade(true).build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF59D)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hi $userName !!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(30.dp))

            // MENU
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: Thông tin
                MenuButton(
                    text = "Cập nhập thông tin của tôi",
                    onClick = onGoToInfo
                )

                // Button 2: Đơn hàng
                MenuButton(
                    text = "Đơn hàng của tôi",
                    onClick = onGoToOrders
                )

                // Button 3: Đổi mật khẩu
                MenuButton(
                    text = "Thay đổi mật khẩu",
                    onClick = onGoToChangePass
                )

                // Button 4: Hỗ trợ (Chưa có chức năng thì để trống hoặc Toast)
                MenuButton(
                    text = "Hỗ trợ",
                    onClick = { /* Todo: Mở trang hỗ trợ */ }
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Cấu hình giao diện của nút
@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}