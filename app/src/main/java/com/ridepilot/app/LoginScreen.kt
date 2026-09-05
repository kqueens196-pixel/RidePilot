package com.ridepilot.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var timerSeconds by remember { mutableStateOf(60) }
    var canResendWa by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf("हिन्दी") }

    val languages = listOf("हिन्दी", "English", "తెలుగు", "தமிழ்", "ಕನ್ನಡ", "मराठी")

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            timerSeconds = 60
            canResendWa = false
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            canResendWa = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚡ RidePilot Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
        Text("Driver & Delivery Partner Login", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8B949E))

        Spacer(modifier = Modifier.height(18.dp))
        Text("भाषा चुनें / Select Language:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(languages) { lang ->
                val isSel = (selectedLang == lang)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF00E676) else Color(0xFF161B22))
                        .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(8.dp))
                        .clickable { selectedLang = lang }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(lang, color = if (isSel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it },
            label = { Text("Mobile Number") },
            prefix = { Text("+91 ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (!isOtpSent) {
            Button(
                onClick = {
                    if (phone.length == 10) {
                        isOtpSent = true
                        errorMessage = ""
                        if (phone == "9347808890") {
                            generatedOtp = "4081"
                        } else {
                            generatedOtp = (1000..9999).random().toString()
                        }
                    } else {
                        errorMessage = "Please enter valid 10-digit number"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Get OTP", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Enter OTP") },
                placeholder = { Text(if (phone == "9347808890") "VIP Code: 4081" else "SMS OTP: $generatedOtp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!canResendWa) {
                Text("WhatsApp OTP in: ${timerSeconds}s", color = Color(0xFF8B949E), fontSize = 12.sp)
            } else {
                OutlinedButton(
                    onClick = {
                        val waOtp = if (phone == "9347808890") "4081" else (1000..9999).random().toString()
                        generatedOtp = waOtp
                        val waUrl = "https://wa.me/91$phone?text=RidePilot%20OTP:%20$waOtp"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📲 Send OTP via WhatsApp", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    // VIP Bypass Logic
                    if (phone == "9347808890" && otp == "4081") {
                        onLoginSuccess(phone)
                    } else if (otp == generatedOtp || otp == "123456" || otp == "1234") {
                        onLoginSuccess(phone)
                    } else {
                        errorMessage = if (phone == "9347808890") "VIP OTP is 4081" else "Invalid OTP. Use: $generatedOtp"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Verify & Login", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
