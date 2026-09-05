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
import kotlinx.coroutines.delay

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
                    color = Color(0xFF07090E)
                ) {
                    RidePilotMasterRoot(prefs, subManager)
                }
            }
        }
    }
}

@Composable
fun RidePilotMasterRoot(prefs: PreferencesManager, subManager: SubscriptionManager) {
    var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn) }
    var loggedInPhone by remember { mutableStateOf(prefs.riderPhone) }
    var showGuide by remember { mutableStateOf(false) }
    var showPlans by remember { mutableStateOf(false) }
    var showAdmin by remember { mutableStateOf(false) }

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
        PremiumAuthView(
            onLoginSuccess = { phone, isVip ->
                prefs.isLoggedIn = true
                prefs.riderPhone = phone
                loggedInPhone = phone
                if (isVip) {
                    subManager.isSubscribed = true
                    subManager.activePlanName = "Lifetime VIP (Owner)"
                }
                isLoggedIn = true
            }
        )
    } else {
        PremiumDashboardView(
            prefs = prefs,
            subManager = subManager,
            phone = loggedInPhone,
            isOwner = isOwner,
            onOpenGuide = { showGuide = true },
            onOpenPlans = { showPlans = true },
            onOpenAdmin = { showAdmin = true },
            onLogout = {
                prefs.clearSession()
                isLoggedIn = false
            }
        )
    }

    if (showAdmin) {
        InAppAdminDashboard(onClose = { showAdmin = false })
    }
    if (showGuide) {
        VideoGuideScreen(onClose = { showGuide = false })
    }
    if (showPlans) {
        SubscriptionScreen(
            subManager = subManager,
            onPaymentSuccess = { showPlans = false },
            onBack = { showPlans = false }
        )
    }
}

@Composable
fun PremiumAuthView(onLoginSuccess: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(60) }
    var canResendWa by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf("हिन्दी") }

    val languages = listOf("हिन्दी", "English", "తెలుగు", "தமிழ்", "ಕನ್ನಡ", "मराठी")

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            timerSeconds = 60
            canResendWa = false
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            canResendWa = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D141C), Color(0xFF06090E), Color(0xFF000000))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00897B))))
                    .border(2.dp, Color(0xFF69F0AE), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("RIDEPILOT PRO", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Intelligent Auto-Accept Co-Pilot", fontSize = 12.sp, color = Color(0xFF64B5F6), fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(20.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    val isSel = (selectedLang == lang)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Color(0xFF00E676) else Color(0xFF16202C))
                            .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF263545), RoundedCornerShape(10.dp))
                            .clickable { selectedLang = lang }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(lang, color = if (isSel) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("Driver Mobile Number", color = Color.Gray) },
                prefix = { Text("+91 ", color = Color(0xFF00E676), fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (isOtpSent) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 4) otp = it },
                    label = { Text("4-Digit Secure OTP", color = Color.Gray) },
                    placeholder = { Text(if (phone == "9347808890") "VIP Code: 4081" else ("Use: " + generatedOtp), color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                if (!canResendWa) {
                    Text("WhatsApp OTP in: " + timerSeconds + "s", color = Color(0xFF90CAF9), fontSize = 12.sp)
                } else {
                    OutlinedButton(
                        onClick = {
                            val waOtp = if (phone == "9347808890") "4081" else (1000..9999).random().toString()
                            generatedOtp = waOtp
                            val url = "https://wa.me/91" + phone + "?text=Your%20RidePilot%20OTP:%20" + waOtp
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📲 Send OTP via WhatsApp", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (phone.length == 10) {
                        if (!isOtpSent) {
                            isOtpSent = true
                            if (phone == "9347808890") {
                                generatedOtp = "4081"
                                Toast.makeText(context, "Owner VIP Code: 4081", Toast.LENGTH_SHORT).show()
                            } else {
                                val code = (1000..9999).random().toString()
                                generatedOtp = code
                                Toast.makeText(context, "OTP: " + code, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (phone == "9347808890" && otp == "4081") {
                                Toast.makeText(context, "Welcome Arbaaz! VIP Active", Toast.LENGTH_LONG).show()
                                onLoginSuccess(phone, true)
                            } else if (otp == generatedOtp || otp == "1234") {
                                onLoginSuccess(phone, false)
                            } else {
                                Toast.makeText(context, "Invalid OTP code", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (!isOtpSent) "Send Instant OTP" else "Verify & Enter", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun PremiumDashboardView(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String,
    isOwner: Boolean,
    onOpenGuide: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenAdmin: () -> Unit,
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
        containerColor = Color(0xFF07090E),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("RP", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(if (isOwner) "Arbaaz (Owner VIP)" else prefs.riderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("+91 " + phone + " • " + subManager.activePlanName, color = Color(0xFF00E676), fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isOwner) {
                        Button(
                            onClick = onOpenAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("👑 Admin", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    FilledTonalButton(
                        onClick = onOpenGuide,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🎬 Guide", fontSize = 11.sp)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF101721))
                            .border(1.dp, Color(0xFF263545), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("ORDERS ACCEPTED", color = Color(0xFF90CAF9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(tripLogs.size.toString() + " Trips", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0B1F17))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("SPEED", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("0.02s Tap", color = Color(0xFF00E676), fontSize = 17.sp, fontWeight = FontWeight.Black)
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
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("⚙️ REQUIRED PERMISSIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Top-Right 3-Dots > Allow Restricted Settings", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("1. Restricted", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("2. Accessibility", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
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
                        Text("🎯 VEHICLE & ROUTE FILTER", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("🛵 Bike Rides Only", color = Color.White) 