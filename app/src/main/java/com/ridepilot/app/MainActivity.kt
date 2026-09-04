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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            RidePilotTheme {
                val context = LocalContext.current
                var currentLang by remember { mutableStateOf(AppLanguage.HI) }
                var showVideoGuide by remember { mutableStateOf(false) }
                var showSubscription by remember { mutableStateOf(false) }
                var showGuideDialog by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    showGuideDialog = true
                }

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

                var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn) }
                var loggedInPhone by remember { mutableStateOf(prefs.riderPhone) }

                when {
                    showVideoGuide -> {
                        VideoGuideScreen(onClose = { showVideoGuide = false })
                    }
                    showSubscription -> {
                        SubscriptionScreen(
                            subManager = subManager,
                            onPaymentSuccess = {
                                showSubscription = false
                                prefs.autoAccept = true
                            },
                            onBack = { showSubscription = false }
                        )
                    }
                    !isLoggedIn -> {
                        DirectRiderLoginScreen(
                            currentLang = currentLang,
                            onLanguageSelected = { currentLang = it },
                            onLoginSuccess = { phone ->
                                prefs.isLoggedIn = true
                                prefs.riderPhone = phone
                                loggedInPhone = phone
                                isLoggedIn = true
                            }
                        )
                    }
                    else -> {
                        DashboardView(
                            prefs = prefs,
                            subManager = subManager,
                            phone = loggedInPhone,
                            currentLang = currentLang,
                            onOpenVideoGuide = { showVideoGuide = true },
                            onOpenSubscription = { showSubscription = true },
                            onLanguageSelected = { currentLang = it },
                            onLogout = {
                                prefs.clearSession()
                                isLoggedIn = false
                            }
                        )
                    }
                }

                if (showGuideDialog) {
                    AlertDialog(
                        onDismissRequest = { showGuideDialog = false },
                        containerColor = Color(0xFF161B22),
                        title = {
                            Text("⚡ Auto-Accept On Karein", color = Color.White, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Android Restricted Setting unlock karein:", color = Color(0xFF8B949E), fontSize = 13.sp)
                                Text("1. 'App Settings' par tap karein", color = Color.White, fontSize = 12.sp)
                                Text("2. Top-Right me 3-Dots (⋮) dabayein", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("3. 'Allow restricted settings' choose karein", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("4. Wapas aakar Accessibility ON karein", color = Color.White, fontSize = 12.sp)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showGuideDialog = false
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                            ) {
                                Text("Open Settings (3-Dots)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showGuideDialog = false }) {
                                Text("Dismiss", color = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DirectRiderLoginScreen(
    currentLang: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡ RidePilot Pro", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E676))
        Text("All Delivery & Cab Auto-Accept", fontSize = 13.sp, color = Color(0xFF8B949E))

        Spacer(modifier = Modifier.height(20.dp))

        Text("Select Language / भाषा चुनें:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AppLanguage.values().toList()) { lang ->
                val isSel = currentLang == lang
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF00E676) else Color(0xFF161B22))
                        .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(8.dp))
                        .clickable { onLanguageSelected(lang) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(lang.nativeName, color = if (isSel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it },
            label = { Text("Mobile Number") },
            prefix = { Text("+91 ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color(0xFF30363D)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isOtpSent) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = enteredOtp,
                onValueChange = { if (it.length <= 4) enteredOtp = it },
                label = { Text("Enter OTP (Auto-fill: 1234)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF30363D)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (phone.length == 10) {
                    if (!isOtpSent) {
                        isOtpSent = true
                        enteredOtp = "1234"
                        Toast.makeText(context, "OTP Sent: 1234 (Auto-Filled)", Toast.LENGTH_SHORT).show()
                    } else {
                        onLoginSuccess(phone)
                    }
                } else {
                    Toast.makeText(context, "10-digit mobile number daalein", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (!isOtpSent) "Send OTP" else "Verify & Login", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardView(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String,
    currentLang: AppLanguage,
    onOpenVideoGuide: () -> Unit,
    onOpenSubscription: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var isGoHomeOn by remember { mutableStateOf(prefs.isGoHomeEnabled) }
    var destText by remember { mutableStateOf(prefs.destinationAddress) }
    var destRadiusKm by remember { mutableStateOf(prefs.destinationRadiusKm) }
    var maxPickupKm by remember { mutableStateOf(prefs.maxPickupKm) }
    val acceptedTrips by remember { mutableStateOf(prefs.getAcceptedTrips()) }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⚡ RidePilot Pro", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("+91 $phone • ${subManager.activePlanName}", color = Color(0xFF00E676), fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onOpenVideoGuide) {
                        Text("🎬 Guide", fontSize = 12.sp)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
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
                            Text("Plan: ${subManager.activePlanName}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Daily ₹10 / 10-Days ₹50 / Monthly ₹99", color = Color(0xFF8B949E), fontSize = 11.sp)
                        }
                        Button(
                            onClick = onOpenSubscription,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text("Plans / Pay", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        Text("Universal support: Rapido, Porter, Swiggy, Zomato, Uber, Zepto, Blinkit", color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val waUrl = "https://wa.me/919347808890?text=Hello%20Arbaaz%2C%20mujhe%20RidePilot%20me%20help%20chahiye"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💬 WhatsApp Support: +91 9347808890", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            item {
                Text("📍 ACCEPTED ORDERS (LIVE LOG)", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (acceptedTrips.isEmpty()) {
                item {
                    Text("Waiting for incoming orders to auto-accept...", color = Color(0xFF8B949E), fontSize = 12.sp)
                }
            } else {
                items(acceptedTrips) { trip ->
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
                            Text("Pickup: ${trip.pickup}", color = Color.White, fontSize = 12.sp)
                            Text("Drop: ${trip.drop}", color = Color(0xFFFFD54F), fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
