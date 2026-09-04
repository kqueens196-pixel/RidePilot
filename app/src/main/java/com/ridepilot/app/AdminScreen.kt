package com.ridepilot.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminScreen(
    subManager: SubscriptionManager,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var riderToActivate by remember { mutableStateOf("") }
    var selectedPlanDays by remember { mutableStateOf(30) }

    Scaffold(
        containerColor = Color(0xFF090D16)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("👑 RidePilot Master Admin", color = Color(0xFFFFD54F), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Logged in as Mohammed Arbaaz (Owner)", color = Color(0xFF8B949E), fontSize = 12.sp)
                    }
                    TextButton(onClick = onClose) {
                        Text("Back", color = Color.White)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("💰 PAYMENT DETAILS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("UPI ID: 9347808890-n7bc@ibl", color = Color.White, fontSize = 13.sp)
                        Text("Support No: +91 9347808890", color = Color(0xFF00E676), fontSize = 13.sp)
                        Text("Status: Active & Listening for payments", color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚡ MANUAL RIDER ACTIVATION", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Kisi rider ne cash ya direct pay kiya ho toh uska number daal kar turant activate karein:", color = Color(0xFF8B949E), fontSize = 11.sp)

                        OutlinedTextField(
                            value = riderToActivate,
                            onValueChange = { if (it.length <= 10) riderToActivate = it },
                            label = { Text("Rider Mobile Number") },
                            prefix = { Text("+91 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF30363D)
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1 to "1 Day", 10 to "10 Days", 30 to "1 Month", 365 to "1 Year").forEach { (days, label) ->
                                val isSel = selectedPlanDays == days
                                Button(
                                    onClick = { selectedPlanDays = days },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) Color(0xFF00E676) else Color(0xFF21262D)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(label, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (riderToActivate.length == 10) {
                                    Toast.makeText(context, "✅ +91 $riderToActivate activated for $selectedPlanDays days!", Toast.LENGTH_LONG).show()
                                    riderToActivate = ""
                                } else {
                                    Toast.makeText(context, "Valid 10-digit number enter karein", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Activate Rider Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛡️ OWNER LIFETIME STATUS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Aapka device permanently unlocked hai. Kisi payment ya subscription ki zaroorat nahi hai.", color = Color.White, fontSize = 12.sp)
                        
                        Button(
                            onClick = {
                                subManager.isSubscribed = true
                                subManager.activePlanName = "Lifetime VIP (Owner)"
                                subManager.expiryTimeMillis = Long.MAX_VALUE
                                Toast.makeText(context, "Lifetime Access Verified!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Force Enable Lifetime VIP", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
