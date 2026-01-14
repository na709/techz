package com.example.techz.model
import com.google.gson.annotations.SerializedName

data class MomoPaymentRequest(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("orderInfo") val orderInfo: String
)

data class MomoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("payUrl") val payUrl: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("message") val message: String?
)