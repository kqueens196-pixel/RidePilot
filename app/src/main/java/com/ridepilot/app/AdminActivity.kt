package com.ridepilot.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

data class DriverUser(
    val phone: String = "",
    val status: String = "Trial",
    val expiry: Long = 0L,
    val isBlocked: Boolean = false
)

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AdminDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var targetPhone by remember { mutableStateOf("") }
    var drivers by remember { mutableStateOf<List<DriverUser>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    DriverUser(
                        phone = doc.id,
                        status = doc.getString("status") ?: "Trial",
                        expiry = doc.getLong("expiry") ?: 0L,
                        isBlocked = doc.getBoolean("isBlocked") ?: false
                    )
                }
                drivers = list
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RidePilot Master Admin", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        containerColor = Color(0xFF090D12)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Manual Driver Plan Activation", color = Color.White, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = targetPhone,
                            onValueChange = { targetPhone = it },
                            placeholder = { Text("Enter 10-digit Phone", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (targetPhone.isNotBlank()) {
                                        val exp = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)
                                        db.collection("users").document(targetPhone).set(
                                            mapOf("status" to "Weekly Pro", "expiry" to exp, "isBlocked" to false)
                                        )
                                        Toast.makeText(context, "Activated 7 Days", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+7D", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    if (targetPhone.isNotBlank()) {
                                        val exp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
                                        db.collection("users").document(targetPhone).set(
                                            mapOf("status" to "Monthly VIP", "expiry" to exp, "isBlocked" to false)
                                        )
                                        Toast.makeText(context, "Activated 30 Days", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6FEB)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+30D", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    if (targetPhone.isNotBlank()) {
                                        db.collection("users").document(targetPhone).set(
                                            mapOf("isBlocked" to true, "status" to "Blocked")
                                        )
                                        Toast.makeText(context, "Driver Blocked", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Block", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text("Registered Drivers (${drivers.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(drivers) { driver ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(driver.phone, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(driver.status, color = if (driver.isBlocked) Color.Red else Color(0xFF00E676), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
