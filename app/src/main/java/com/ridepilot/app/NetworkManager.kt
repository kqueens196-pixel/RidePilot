package com.ridepilot.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {
    // Android emulator ke liye 10.0.2.2 ya physical device ke liye localhost / LAN IP
    private val baseUrl = "http://127.0.0.1:3000"

    suspend fun requestOtp(phone: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/auth/send-otp")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val payload = JSONObject().put("phone", phone)
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            conn.responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/auth/verify-otp")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val payload = JSONObject().apply {
                put("phone", phone)
                put("otp", otp)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            conn.responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchOrders(): List<NormalizedOrder> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/orders/feed")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                val json = JSONObject(response)
                val ordersArray = json.getJSONArray("orders")
                val list = mutableListOf<NormalizedOrder>()

                for (i in 0 until ordersArray.length()) {
                    val obj = ordersArray.getJSONObject(i)
                    list.add(
                        NormalizedOrder(
                            id = obj.getString("id"),
                            provider = obj.getString("provider"),
                            type = if (obj.getString("type") == "RIDE") OrderType.RIDE else OrderType.PARCEL,
                            pickupAddress = obj.getString("pickup"),
                            dropAddress = obj.getString("drop"),
                            distanceKm = obj.getDouble("distanceKm"),
                            payoutInr = obj.getDouble("payoutInr")
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
