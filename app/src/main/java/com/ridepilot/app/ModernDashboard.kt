package com.ridepilot.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridepilot.app.PreferencesManager
import com.ridepilot.app.AdminActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboard(
    prefs: PreferencesManager,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF161B22),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Earnings") },
                    label = { Text("Earnings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(prefs = prefs)
                1 -> EarningsScreen()
                2 -> OrdersScreen()
                3 -> ProfileScreen(prefs = prefs, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun HomeScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var floatingBubble by remember { mutableStateOf(prefs.floatingBubble) }
    var pickupDist by remember { mutableStateOf(prefs.maxPickupDistance) }
    var dropDist by remember { mutableStateOf(prefs.maxDropDistance) }
    var bikeMode by remember { mutableStateOf(prefs.bikeMode) }
    var parcelMode by remember { mutableStateOf(prefs.parcelMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("RidePilot Auto-Accept", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Ola • Rapido • Uber • Porter", fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                color = if (autoAccept) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (autoAccept) "ONLINE" else "OFFLINE",
                    color = if (autoAccept) Color(0xFF00E676) else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Master Auto-Accept", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Auto swipe/click orders instantly", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = autoAccept,
                    onCheckedChange = {
                        autoAccept = it
                        prefs.autoAccept = it
                    }
                )
            }
        }

        // Floating Bubble Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Floating Drive Bubble", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Show widget over Ola / Rapido screen", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = floatingBubble,
                    onCheckedChange = {
                        floatingBubble = it
                        prefs.floatingBubble = it
                    }
                )
            }
        }

        // Distance Filters Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Pickup Filter (200m - 500m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Max Pickup Distance", fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Nearest rider limit (200m - 500m)", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        text = "${(pickupDist * 1000).toInt()} M",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
                Slider(
                    value = pickupDist,
                    onValueChange = {
                        pickupDist = it
                        prefs.maxPickupDistance = it
                    },
                    valueRange = 0.2f..0.5f,
                    steps = 2
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF21262D))
                Spacer(modifier = Modifier.height(12.dp))

                // Drop Distance Filter (2km - 25km)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Max Drop / Trip Distance", fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Trip destination limit", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        text = "${dropDist.toInt()} KM",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
                Slider(
                    value = dropDist,
                    onValueChange = {
                        dropDist = it
                        prefs.maxDropDistance = it
                    },
                    valueRange = 2.0f..25.0f,
                    steps = 22
                )
            }
        }

        // Vehicle Modes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target Vehicle Modes", fontWeight = FontWeight.SemiBold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bike Ride Mode", color = Color.White)
                    Checkbox(
                        checked = bikeMode,
                        onCheckedChange = {
                            bikeMode = it
                            prefs.bikeMode = it
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Parcel / Delivery Mode", color = Color.White)
                    Checkbox(
                        checked = parcelMode,
                        onCheckedChange = {
                            parcelMode = it
                            prefs.parcelMode = it
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E7E34)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enable Accessibility Service", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun EarningsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Today's Earnings", fontSize = 16.sp, color = Color.Gray)
        Text("₹0.00", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
    }
}

@Composable
fun OrdersScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No Trips Accepted Today", color = Color.Gray)
    }
}

@Composable
fun ProfileScreen(
    prefs: PreferencesManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var secretClickCount by remember { mutableStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }
    var adminPin by remember { mutableStateOf("") }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Master Admin Access", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter Master Security PIN:", color = Color.Gray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = adminPin,
                        onValueChange = { adminPin = it },
                        placeholder = { Text("PIN") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPin == "7860") {
                            showPinDialog = false
                            adminPin = ""
                            val intent = Intent(context, AdminActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Invalid Master PIN", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Login", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF161B22)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Account & Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.clickable {
                secretClickCount++
                if (secretClickCount >= 5) {
                    secretClickCount = 0
                    showPinDialog = true
                }
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pilot Member", fontWeight = FontWeight.Bold, color = Color.White)
                Text(prefs.userPhone.ifEmpty { "+91 9876543210" }, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Status: " + if (prefs.isSubscriptionActive) "Active Member" else "Trial / Expired", color = Color.White)
            }
        }

        Text("Subscription Plans", fontWeight = FontWeight.Bold, color = Color.White)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanCard(modifier = Modifier.weight(1f), title = "Weekly", price = "₹49", duration = "7 Days") {
                openUPI(context, "49", "Weekly Plan")
            }
            PlanCard(modifier = Modifier.weight(1f), title = "Monthly", price = "₹149", duration = "Best Value", isPopular = true) {
                openUPI(context, "149", "Monthly Plan")
            }
            PlanCard(modifier = Modifier.weight(1f), title = "VIP Pass", price = "₹349", duration = "90 Days") {
                openUPI(context, "349", "VIP Pass Plan")
            }
        }

        Button(
            onClick = {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("App Permissions & Overlay", fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
            onClick = {
                prefs.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reset Session / Logout", color = Color.White)
        }
    }
}

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    duration: String,
    isPopular: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPopular) Color(0xFF0F3821) else Color(0xFF161B22)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Text(price, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(duration, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

fun openUPI(context: Context, amount: String, note: String) {
    val upiUri = Uri.parse("upi://pay?pa=9347808890-n7bc@ibl&pn=RidePilot&am=$amount&cu=INR&tn=$note")
    val intent = Intent(Intent.ACTION_VIEW, upiUri)
    try {
        context.startActivity(Intent.createChooser(intent, "Pay with UPI"))
    } catch (_: Exception) {
        Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
    }
}
