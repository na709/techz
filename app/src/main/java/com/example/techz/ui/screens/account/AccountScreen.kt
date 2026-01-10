package com.example.techz.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onGoToLogin: () -> Unit,
    onLogout: () -> Unit,
    onGoToRegister: () -> Unit
) {
    // Màu thương hiệu
    val brandColor = Color(0xFF0066FF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TechZ Store",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    // Chỉ nên hiện nút Logout khi user đã đăng nhập.
                    // Tạm thời mình để đây, bạn có thể bọc nó trong câu lệnh if (isLoggedIn)
                    IconButton(onClick = { onLogout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brandColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Thêm Icon lớn để giao diện đỡ trống trải
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://dvna.site/images/logo.jpg")
                    //.data("http://160.250.247.5/images/logo.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = "Banner Sales",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Chào mừng bạn đến với\nTechZ Store",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Vui lòng đăng nhập để mua sắm và nhận ưu đãi",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Khu vực Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Nút Đăng nhập (Filled Button)
                Button(
                    onClick = onGoToLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandColor,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Đăng nhập", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nút Đăng ký (Outlined Button)
                OutlinedButton(
                    onClick = onGoToRegister,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = brandColor
                    ),
                    border = BorderStroke(1.5.dp, brandColor),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Đăng ký", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}