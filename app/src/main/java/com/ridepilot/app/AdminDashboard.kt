package com.ridepilot.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LiveDriverItem(
    val phone: String,
    val plan: String,
    val amount: Int,
    val active: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppAdminDashboard(onClose: () -> Unit) {
    val drivers = remember {
        mutableStateListOf(
            LiveDriverItem("9347808890", "Lifetime VIP (Owner)", 0, true),
            LiveDriverItem("9848022338", "Monthly Pass", 99, true),
            LiveDriverItem("9123456789", "10-Days Pass", 50, false),
            LiveDriverItem("9988776655", "Daily Pass", 10, true)
        )
    }

    var totalRevenue = 0
    var activeCount = 0
    for (d in drivers) {
        totalRevenue += d.amount
        if (d.active) {
            activeCount += 1
        }
    }

    Scaffold(
        containerColor = Color(0xFF07090E),
        topBar = {
            TopAppBar(
                title = { Text("⚡ Master Admin Portal", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101721)),
                actions = {
                    TextButton(onClick = onClose) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF101721))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Drivers", color = Color.Gray, fontSize = 10.sp)
                            Text(drivers.size.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1F17))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Active VIP", color = Color(0xFF00E676), fontSize = 10.sp)
                            Text(activeCount.toString(), color = Color(0xFF00E676), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2211))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sales", color = Color(0xFFFFD54F), fontSize = 10.sp)
                            Text("₹" + totalRevenue.toString(), color = Color(0xFFFFD54F), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("REGISTERED DRIVERS & MEMBERSHIP CONTROLLER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            items(drivers) { u ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF263545), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101721))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("+91 " + u.phone, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(u.plan + " • ₹" + u.amount, color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val idx = drivers.indexOf(u)
                                if (idx != -1) {
                                    drivers[idx] = u.copy(active = !u.active)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (u.active) Color(0xFFFF5252) else Color(0xFF00E676)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (u.active) "Block" else "Activate VIP",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
