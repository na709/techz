package com.example.techz.ui.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.techz.model.AuthResponse // File model vừa tạo
import com.example.techz.model.LoginRequest // File model request
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit,
                onClickRegister: () -> Unit) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    //val logoUrl = "http://160.250.247.5/images/logo.jpg"
    val logoUrl = "https://dvna.site/images/logo.jpg"
    val brandColor = Color(0xFF0066FF)


    fun handleLogin() {
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(username = username, password = password)
        RetrofitClient.instance.loginUser(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {

                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()
                    val user = authData?.user
                    val role = authData?.role ?:"user"
                    user?.name?.let {
                        UserSession.login(context, it,role)
                    }
                    Toast.makeText(context, "Xin chào ${user?.name}!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(role)
                } else {
                    Toast.makeText(context, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Lỗi kết nối: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Logo TechZ",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        Text("TECHZ STORE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = brandColor)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { handleLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brandColor),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("ĐANG XỬ LÝ...")
            } else {
                Text("ĐĂNG NHẬP")
            }
        }
        Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center, // Căn giữa toàn bộ dòng
            verticalAlignment = Alignment.CenterVertically // Căn giữa theo chiều dọc
        ) {
            Text(
                text = "Chưa có tài khoản?",
                fontSize = 14.sp,
                color = Color.Gray
            )

            TextButton(
                onClick = { onClickRegister() },
                contentPadding = PaddingValues(start = 4.dp)
            ) {
                Text(
                    text = "Đăng ký ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandColor
                )
            }
        }
    }
}