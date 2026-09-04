package com.ridepilot.app

import androidx.compose.foundation.layout.*
import androidx.sompose.material3.*
import androidx.sompose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf(6") }
    var isOtpSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RidePilot Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))


       if (!isOtpSent) {
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("Mobile Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (phone.length == 10) isOtpSent = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.length == 10
            ) {
                Text("Get OTP")
            }
        } else {
            Text("Enter OTP sent to +91 $phone", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("6-Digit OTP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (otp.length == 6) {
                        onLoginSuccess(phone)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6
            ) {
                Text("Verify & Continue")
            }
        }
    }
}
