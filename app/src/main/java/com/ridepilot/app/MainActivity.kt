package com.ridepilot.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PreferencesManager
    private lateinit var subManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(applicationContext)
        subManager = SubscriptionManager(applicationContext)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    MainScreenContent(prefs, subManager)
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(prefs: PreferencesManager, subManager: SubscriptionManager) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn) }
    var loggedInPhone by remember { mutableStateOf(prefs.riderPhone) }
    var showVideoGuide by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    val isOwner = (loggedInPhone == "9347808890")
    if (isOwner) {
        subManager.isSubscribed = true
        subManager.activePlanName = "Lifetime VIP (Owner)"
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { phone ->
                prefs.isLoggedIn = true
                prefs.riderPhone = phone
                loggedInPhone = phone
                if (phone == "9347808890") {
                    subManager.isSubscribed = true
                    subManager.activePlanName = "Lifetime VIP (Owner)"
                }
                isLoggedIn = true
            }
        )
    } else {
        MainDashboardUI(
            prefs = prefs,
            subManager = subManager,
            phone = loggedInPhone,
            isOwner = isOwner,
            onOpenGuide = { showVideoGuide = true },
            onOpenPlans = { showSubscription = true },
            onLogout = {
                prefs.clearSession()
                isLoggedIn = false
            }
        )
    }

    if (showVideoGuide) {
        VideoGuideScreen(onClose = { showVideoGuide = false })
    }
    if (showSubscription) {
        SubscriptionScreen(
            subManager = subManager,
            onPaymentSuccess = { showSubscription = false },
            onBack = { showSubscription = false }
        )
    }
}

@Composable
fun MainDashboardUI(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String,
    isOwner: Boolean,
    onOpenGuide: () -> Unit,
    onOpenPlans: () -> Unit,
    onLogout: () -> Unit
) {
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var isRideOnly by remember { mutableStateOf(prefs.isRideEnabled) }
    var isParcelOnly by remember { mutableStateOf(prefs.isParcelEnabled) }
    var isCombo by remember { mutableStateOf(prefs.isComboRouteEnabled) }
    var maxKm by remember { mutableStateOf(prefs.maxPickupKm) }
    val tripLogs = prefs.getAcceptedTrips()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(prefs.riderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("+91 " + phone + " • " + subManager.activePlanName, color = Color(0xFF00E676), fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onOpenGuide) { Text("🎬 Guide", fontSize = 11.sp) }
                    TextButton(onClick = onLogout) { Text("Logout", color = Color(0xFFFF5252), fontSize = 12.sp) }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Plan Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16251E)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E676), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Plan: " + subManager.activePlanName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (isOwner) "Owner VIP Active (Lifetime Free)" else "Daily ₹10 / 10-Days ₹50 / Monthly ₹99", color = Color(0xFF8B949E), fontSize = 11.sp)
                        }
                        if (!isOwner) {
                            Button(
                                onClick = onOpenPlans,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                            ) {
                                Text("Plans / Pay", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Permissions Quick Access Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚙️ REQUIRED PERMISSIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS").apply {
                                            data = Uri.parse("package:" + context.packageName)
                                        }
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Top-Right 3-Dots > Allow Restricted Settings", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("1. Restricted Settings", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("2. Accessibility ON", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Order Filters (Bike Only / Parcel Only / Combo & Distance)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🎯 ORDER FILTERS & ROUTING", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Bike Rides Only", color = Color.White, fontSize = 13.sp)
                            Switch(checked = isRideOnly, onCheckedChange = { isRideOnly = it; prefs.isRideEnabled = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Parcel Deliveries Only", color = Color.White, fontSize = 13.sp)
                            Switch(checked = isParcelOnly, onCheckedChange = { isParcelOnly = it; prefs.isParcelEnabled = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Combo Route (Match Nearby Drop)", color = Color.White, fontSize = 13.sp)
                            Switch(checked = isCombo, onCheckedChange = { isCombo = it; prefs.isComboRouteEnabled = it })
                        }

                        Divider(color = Color(0xFF30363D), thickness = 1.dp)

                        Text("Max Drop/Pickup Radius: " + maxKm + " KM", color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf(1.0f, 2.0f, 3.0f, 5.0f).forEach { km ->
                                OutlinedButton(
                                    onClick = { maxKm = km; prefs.maxPickupKm = km },
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (maxKm == km) Color(0xFF00E676) else Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(km.toInt().toString() + " KM", fontSize = 11.sp, color = if (maxKm == km) Color.Black else Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Auto-Accept Switch Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡ Instant Auto-Accept", color = Color.White, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = autoAccept,
                                onCheckedChange = {
                                    autoAccept = it
                                    prefs.autoAccept = it
                                }
                            )
                        }
                        Text("Universal: Rapido, Porter, Swiggy, Zomato, Uber, Zepto, Blinkit", color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                }
            }

            // Live Order Logs with Earnings & Address Banner
            item {
                Text("📍 ACCEPTED ORDERS (LIVE EARNINGS LOG)", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (tripLogs.isEmpty()) {
                item {
                    Text("No orders accepted yet. Waiting for incoming requests...", color = Color(0xFF8B949E), fontSize = 12.sp)
                }
            } else {
                items(tripLogs) { trip ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16251E)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(trip.provider, color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(trip.fare, color = Color(0xFF00E676), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                            Text("🟢 Pickup: " + trip.pickup, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("🔴 Drop: " + trip.drop, color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
