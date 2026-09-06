package com.ridepilot.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernDashboardView(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String,
    isOwner: Boolean,
    onOpenGuide: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Home") }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var selectedVehicle by remember { mutableStateOf("Bike") }
    var maxKm by remember { mutableStateOf(prefs.maxPickupKm) }
    var parcelOnly by remember { mutableStateOf(prefs.isParcelEnabled) }

    val tabs = listOf("Home", "Earnings", "Orders", "Profile")
    val tripLogs = prefs.getAcceptedTrips()

    Scaffold(
        containerColor = Color(0xFF070B11),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0B111A), tonalElevation = 8.dp) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = (activeTab == tab),
                        onClick = { activeTab = tab },
                        label = { Text(tab, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            val iconText = when (tab) {
                                "Home" -> "🏠"
                                "Earnings" -> "📊"
                                "Orders" -> "📦"
                                else -> "👤"
                            }
                            Text(iconText, fontSize = 18.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E676),
                            selectedTextColor = Color(0xFF00E676),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF16222F)
                        )
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("RIDEPILOT", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("ONE APP. EVERY RIDE.", fontSize = 10.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isOwner) {
                            Button(
                                onClick = onOpenAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("👑 Admin", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        FilledTonalButton(
                            onClick = onOpenGuide,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🎬 Guide", fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF101721)).border(1.dp, Color(0xFF263545), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("TODAY'S EARNINGS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹1,245", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF101721)).border(1.dp, Color(0xFF263545), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("ORDERS ACCEPTED", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${tripLogs.size} Trips", color = Color(0xFF00E676), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101721)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF263545), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AUTO ACCEPT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text("Orders accepted automatically", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoAccept,
                            onCheckedChange = { autoAccept = it; prefs.autoAccept = it }
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101721)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SMART ORDER FILTER", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Text("Select Distance", color = Color.Gray, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1.0f, 2.0f, 5.0f, 10.0f).forEach { km ->
                                val isSel = (maxKm == km)
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF00E676) else Color(0xFF16202C))
                                        .clickable { maxKm = km; prefs.maxPickupKm = km }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${km.toInt()} KM", color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select Vehicle Type", color = Color.Gray, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Bike" to "🛵", "Auto" to "🛺", "Car" to "🚗", "Delivery" to "📦").forEach { (type, icon) ->
                                val isSel = (selectedVehicle == type)
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF00E676) else Color(0xFF16202C))
                                        .clickable { selectedVehicle = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(icon, fontSize = 14.sp)
                                        Text(type, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Parcel Mode Only", color = Color.White, fontSize = 13.sp)
                            Switch(checked = parcelOnly, onCheckedChange = { parcelOnly = it; prefs.isParcelEnabled = it })
                        }
                    }
                }
            }

            item {
                Text("LIVE ORDERS (SCANNING)", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            items(tripLogs) { trip ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1F17)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(trip.provider.uppercase(), color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(trip.fare, color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        Text("🟢 ${trip.pickup}", color = Color.White, fontSize = 12.sp)
                        Text("🔴 ${trip.drop}", color = Color(0xFFFFD54F), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
