package com.example.techz.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getCategoryIcon(category: String): ImageVector {

    return when (category.lowercase()) {
        "laptop" -> Icons.Default.Laptop
        "pc", "case", "maytinh" -> Icons.Default.Computer
        "manhinh", "monitor" -> Icons.Default.Monitor
        "chuot", "mouse" -> Icons.Default.Mouse
        "banphim", "keyboard" -> Icons.Default.Keyboard
        "tainghe", "headphone", "micro" -> Icons.Default.Headphones
        "webcam" -> Icons.Default.Videocam
        "ghe", "gamingchair" -> Icons.Default.Chair
        "ram", "ssd", "hdd", "vga", "mainboard", "cpu", "nguon", "tannhiet" -> Icons.Default.Memory
        "thietbimang", "wifi", "router" -> Icons.Default.Router
        else -> Icons.Default.DevicesOther
    }
}
fun getCategoryName(rawName: String?): String {
    if (rawName == null) return "Sản phẩm"
    return when (rawName.lowercase()) {
        "manhinh" -> "Màn Hình"
        "chuot" -> "Chuột"
        "banphim" -> "Bàn Phím"
        "tainghe" -> "Tai Nghe"
        "thietbimang" -> "Wifi/Mạng"
        "linhkien" -> "Linh Kiện"
        "case" -> "Vỏ Case"
        "nguon" -> "Nguồn"
        "tannhiet" -> "Tản Nhiệt"
        "ghe" -> "Ghế Game"
        else -> rawName
    }
}