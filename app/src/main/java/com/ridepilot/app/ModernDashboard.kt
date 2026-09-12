package com.ridepilot.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernDashboard(
    prefs: PreferencesManager,
    phone: String = "9876543210",
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var maxKm by remember { mutableStateOf(prefs.maxPickupKm) }
    var isParcel by remember { mutableStateOf(prefs.isParcelEnabled) }
    var isRide by remember { mutableStateOf(prefs.isRideEnabled) }
    var trips by remember { mutableStateOf(prefs.getAcceptedTrips()) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0D1117)) {
                val items = listOf(
                    Triple("Home", Icons.Default.Home, 0),
                    Triple("Earnings", Icons.Default.Star, 1),
                    Triple("Orders", Icons.Default.CheckCircle, 2),
                    Triple("Profile", Icons.Default.Person, 3)
                )
                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 1 || index == 2) {
                                trips = prefs.getAcceptedTrips()
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E676),
                            selectedTextColor = Color(0xFF00E676),
                            unselectedIconColor = Color(0xFF8B949E),
                            unselectedTextColor = Color(0xFF8B949E),
                            indicatorColor = Color(0xFF161B22)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFF090D12)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(
                    context = context,
                    autoAccept = autoAccept,
                    onAutoAcceptChange = {
                        autoAccept = it
                        prefs.autoAccept = it
                    },
                    maxKm = maxKm,
                    onMaxKmChange = {
                        maxKm = it
                        prefs.maxPickupKm = it
                    },
                    isParcel = isParcel,
                    onParcelChange = {
                        isParcel = it
                        prefs.isParcelEnabled = it
                    },
                    isRide = isRide,
                    onRideChange = {
                        isRide = it
                        prefs.isRideEnabled = it
                    }
                )
                1 -> EarningsScreen(trips = trips)
                2 -> OrdersScreen(trips = trips)
                3 -> ProfileScreen(
                    context = context,
                    phone = phone,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    context: Context,
    autoAccept: Boolean,
    onAutoAcceptChange: (Boolean) -> Unit,
    maxKm: Float,
    onMaxKmChange: (Float) -> Unit,
    isParcel: Boolean,
    onParcelChange: (Boolean) -> Unit,
    isRide: Boolean,
    onRideChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("RidePilot Auto-Accept", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Ola • Rapido • Uber • Porter", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (autoAccept) Color(0xFF00E676) else Color(0xFFFF5252))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(if (autoAccept) "ACTIVE" else "OFFLINE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Master Auto-Accept", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Auto swipe/click orders instantly", color = Color(0xFF8B949E), fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoAccept,
                        onCheckedChange = onAutoAcceptChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E676),
                            checkedTrackColor = Color(0xFF0B3818)
                        )
                    )
                }
            }
        }

        item {
            var bubbleActive by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating Drive Bubble", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Show widget over Ola / Rapido screen", color = Color(0xFF8B949E), fontSize = 12.sp)
                    }
                    Switch(
                        checked = bubbleActive,
                        onCheckedChange = { active ->
                            if (active) {
                                if (Settings.canDrawOverlays(context)) {
                                    context.startService(Intent(context, FloatingBubbleService::class.java))
                                    bubbleActive = true
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    )
                                    context.startActivity(intent)
                                }
                            } else {
                                context.stopService(Intent(context, FloatingBubbleService::class.java))
                                bubbleActive = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E676),
                            checkedTrackColor = Color(0xFF0B3818)
                        )
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Pickup Distance", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", maxKm)} KM", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = maxKm,
                        onValueChange = onMaxKmChange,
                        valueRange = 0.5f..10.0f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E676),
                            activeTrackColor = Color(0xFF00E676)
                        )
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Vehicle Modes", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bike Ride Mode", color = Color.White, fontSize = 13.sp)
                        Checkbox(checked = isRide, onCheckedChange = onRideChange)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parcel / Delivery Mode", color = Color.White, fontSize = 13.sp)
                        Checkbox(checked = isParcel, onCheckedChange = onParcelChange)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Accessibility Service", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EarningsScreen(trips: List<AcceptedTrip>) {
    val totalEarnings = trips.sumOf { trip ->
        trip.fare.replace("₹", "").trim().toIntOrNull() ?: 0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Earnings Overview", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Captured Revenue", color = Color(0xFF8B949E), fontSize = 13.sp)
                    Text("₹$totalEarnings", color = Color(0xFF00E676), fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${trips.size} Total Auto-Accepted Rides", color = Color.White, fontSize = 12.sp)
                }
            }
        }
        item {
            Text("Recent Earnings Breakdown", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (trips.isEmpty()) {
            item {
                Text("No rides accepted yet. Turn on Auto-Accept!", color = Color(0xFF8B949E), fontSize = 13.sp)
            }
        } else {
            items(trips) { trip ->
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
                            Text(trip.provider, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(trip.pickup, color = Color(0xFF8B949E), fontSize = 11.sp)
                        }
                        Text(trip.fare, color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(trips: List<AcceptedTrip>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Order History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        if (trips.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No orders logged yet", color = Color(0xFF8B949E))
                }
            }
        } else {
            items(trips) { trip ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(trip.provider, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                            Text(trip.fare, color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Text(trip.pickup, color = Color.White, fontSize = 12.sp)
                        Text(trip.drop, color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

fun openUpiPayment(context: Context, amount: String, planName: String) {
    try {
        val uri = Uri.parse("upi://pay?pa=ridepilot@upi&pn=RidePilot&tn=Subscription_" + planName + "&am=" + amount + "&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(Intent.createChooser(intent, "Pay with UPI"))
    } catch (_: Exception) {}
}

@Composable
fun ProfileScreen(
    context: Context,
    phone: String,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Account & Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pilot Member", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("+91 $phone", color = Color(0xFF8B949E), fontSize = 13.sp)
                    val statusText = if (prefs.isPremiumActive()) "Status: Active Member" else "Status: Plan Expired"
                    Text(statusText, color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Text("Subscription Plans", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Weekly Plan
                Card(
                    modifier = Modifier.weight(1f).clickable { openUpiPayment(context, "49", "Weekly") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Weekly", color = Color(0xFF8B949E), fontSize = 12.sp)
                        Text("₹49", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("7 Days", color = Color(0xFF00E676), fontSize = 10.sp)
                    }
                }
                // Monthly Plan
                Card(
                    modifier = Modifier.weight(1f).clickable { openUpiPayment(context, "149", "Monthly") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3B22))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monthly", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("₹149", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Best Value", color = Color(0xFF00E676), fontSize = 10.sp)
                    }
                }
                // Quarterly VIP Plan
                Card(
                    modifier = Modifier.weight(1f).clickable { openUpiPayment(context, "349", "VIP") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VIP Pass", color = Color(0xFF8B949E), fontSize = 12.sp)
                        Text("₹349", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("90 Days", color = Color(0xFF00E676), fontSize = 10.sp)
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6FEB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("App Permissions & Overlay", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Session / Logout")
            }
        }
    }
}
