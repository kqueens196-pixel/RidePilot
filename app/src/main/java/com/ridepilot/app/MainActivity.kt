package com.ridepilot.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn) }
    var loggedInPhone by remember { mutableStateOf(prefs.riderPhone) }
    var showVideoGuide by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }

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
            // VIP Plan Card
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

            // Permissions Card (DIRECT 1-CLICK ACTIONS)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("⚙️ REQUIRED PERMISSIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Auto-accept setup karne ke liye dono permissions allow karein:", color = Color(0xFF8B949E), fontSize = 11.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Button 1: Restricted Settings unlock
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Top-Right 3-Dots > Allow Restricted Settings karein", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("1. Restricted", fontSize = 12.sp, color = Color.White)
                            }

                            // Button 2: Accessibility ON
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("2. Accessibility", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
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
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            // WhatsApp Support Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val waUrl = "https://wa.me/919347808890?text=Hello%20Arbaaz%2C%20RidePilot%20help%20chahiye"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💬 WhatsApp Support: +91 9347808890", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live Order Logs
            item {
                Text("📍 ACCEPTED ORDERS (LIVE LOG)", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            items(tripLogs) { trip ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16251E)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(trip.provider, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                            Text(trip.fare, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("Pickup: " + trip.pickup, color = Color.White, fontSize = 12.sp)
                        Text("Drop: " + trip.drop, color = Color(0xFFFFD54F), fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
