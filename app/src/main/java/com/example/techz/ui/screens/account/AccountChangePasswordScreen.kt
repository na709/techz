package com.example.techz.ui.screens.account


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.techz.model.AuthResponse
import com.example.techz.model.ChangePasswordRequest
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import com.example.techz.ui.components.TechZBottomBar
import com.example.techz.ui.navigation.Screen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountChangePasswordScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val brandColor = Color(0xFF00A9FF)
    val userName = UserSession.currentUserName // Lấy tên từ Session cho BottomBar

    // State cho các ô nhập liệu
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Hàm xử lý
    fun handleChangePassword() {
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Gọi API đổi mật khẩu
        isLoading = true

        val request = ChangePasswordRequest(id = userId,oldPassword = oldPassword,newPassword = newPassword)

        RetrofitClient.instance.changePassword(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                isLoading = false

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show()
                    UserSession.logout(context)
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                } else {
                    val msg = response.body()?.message ?: "Đổi mật khẩu thất bại"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Lỗi kết nối: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đổi mật khẩu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brandColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = { TechZBottomBar(navController, userName) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tạo mật khẩu mới",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = brandColor
            )

            Text(
                text = "Mật khẩu mới phải khác mật khẩu trước đó",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            PasswordInputRefactored(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = "Mật khẩu hiện tại",
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputRefactored(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Mật khẩu mới",
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputRefactored(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Nhập lại mật khẩu mới",
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { handleChangePassword() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PasswordInputRefactored(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}