package com.example.techz.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.techz.model.AuthResponse
import com.example.techz.model.UpdateProfileRequest
import com.example.techz.service.RetrofitClient
import com.example.techz.service.UserSession
import com.example.techz.ui.components.TechZBottomBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val brandColor = Color(0xFF00A9FF)

    var name by remember { mutableStateOf(UserSession.currentUserName ?: "") }
    var phone by remember { mutableStateOf(UserSession.currentUserPhone ?: "") }
    var address by remember { mutableStateOf(UserSession.currentUserAddress ?: "") }
    var email by remember { mutableStateOf(UserSession.currentUserEmail ?: "") }

    var isLoading by remember { mutableStateOf(false) }

    // Hàm xử lý lưu thông tin
    fun handleSave() {
        // 1. Validate
        if (name.isBlank() || phone.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên và số điện thoại", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = UserSession.currentUserId
        if (userId == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        // call api
        isLoading = true
        val request = UpdateProfileRequest(id = userId, name = name, phone = phone, address = address)

        RetrofitClient.instance.updateProfile(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body()?.success == true) {

                    UserSession.updateSession(context, name, phone, address)

                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
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
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
        bottomBar = { TechZBottomBar(navController, UserSession.currentUserName) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).background(brandColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://s3.cloudfly.vn/techz-avatar/avatar.jpg")
                            .crossfade(true).build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chạm để đổi ảnh", fontSize = 12.sp, color = brandColor, fontWeight = FontWeight.Medium)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                InfoInputField(
                    label = "Họ và tên",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Default.Person,
                    imeAction = ImeAction.Next)

                InfoInputField(
                    label = "Email",
                    value = email,
                    onValueChange = {},
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isReadOnly = true)

                InfoInputField(
                    label = "Số điện thoại",
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it },
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next)

                InfoInputField(
                    label = "Địa chỉ nhận hàng",
                    value = address,
                    onValueChange = { address = it },
                    icon = Icons.Default.Home,
                    imeAction = ImeAction.Done,
                    isSingleLine = false)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
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
}

@Composable
fun InfoInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    isSingleLine: Boolean = true,
    isReadOnly: Boolean = false // muốn cho nhập email thì sửa thành true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isReadOnly) Color.LightGray else Color.Gray
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = isSingleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),

        readOnly = isReadOnly,

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isReadOnly) Color.LightGray else Color(0xFF00A9FF),
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = if (isReadOnly) Color(0xFFF5F5F5) else Color.Transparent,
            unfocusedContainerColor = if (isReadOnly) Color(0xFFF5F5F5) else Color.Transparent
        )
    )
}