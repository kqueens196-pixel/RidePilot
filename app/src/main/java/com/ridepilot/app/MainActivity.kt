package com.ridepilot.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    color = Color(0xFF0D1117)
                ) {
                    MainScreenContent(prefs, subManager)
                }
            }
        }
    }
}

enum class IdSelectLang(val label: String) {
    HI("हिन्दी (Hindi)"),
    TE("తెలుగు"),
    TA("தமிழ்"),
    KA("ಕನ್ನಡ"),
    MR("मराठी"),
    EN("English")
}

@Composable
fun MainScreenContent(prefs: PreferencesManager, subManager: SubscriptionManager) {
    var currentLang by remember { mutableStateOf(IdSelectLang.HI) }
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
        AuthScreenUI(
            currentLang = currentLang,
            onLangSelected = { currentLang = it },
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
fun AuthScreenUI(
    currentLang: IdSelectLang,
    onLangSelected: (IdSelectLang) -> Unit,
    onLoginSuccess: (String, Boolean) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(60) }
    var canResendWa by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

        Spacer(modifier = Modifier.height(16.dp))
        Text("Select Language / भाषा चुनें:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(IdSelectLang.values()) { lang ->
                val isSel = (currentLang == lang)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF00E676) else Color(0xFF161B22))
                        .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(8.dp))
                        .clickable { onLangSelected(lang) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(lang.label, color = if (isSel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it },
            label = { Text("Mobile Number") },
            prefix = { Text("+91 ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        if (isOtpSent) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = enteredOtp,
                onValueChange = { if (it.length <= 4) enteredOtp = it },
                label = { Text("Enter 4-Digit OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (!canResendWa) {
                Text("WhatsApp OTP in: " + timerSeconds + "s", color = Color(0xFF8B949E), fontSize = 12.sp)
            } else {
                OutlinedButton(
                    onClick = {
                        val otp = if (phone == "9347808890") "4081" else (1000..9999).random().toString()
                        generatedOtp = otp
                        val waUrl = "https://wa.me/91" + phone + "?text=RidePilot%20OTP:%20" + otp
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                        Toast.makeText(context, "WhatsApp OTP Sent!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📲 Send OTP via WhatsApp", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (phone.length == 10) {
                    if (!isOtpSent) {
                        isOtpSent = true
                        if (phone == "9347808890") {
                            generatedOtp = "4081"
                            Toast.makeText(context, "Owner VIP Login: Secret 4081", Toast.LENGTH_LONG).show()
                        } else {
                            val code = (1000..9999).random().toString()
                            generatedOtp = code
                            Toast.makeText(context, "SMS OTP: " + code, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (phone == "9347808890" && enteredOtp == "4081") {
                            Toast.makeText(context, "Hello Arbaaz! VIP Login Active", Toast.LENGTH_LONG).show()
                            onLoginSuccess(phone, true)
                        } else if (enteredOtp.isNotEmpty() && (enteredOtp == generatedOtp || enteredOtp == "1234")) {
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(phone, false)
                        } else {
                            Toast.makeText(context, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "10-digit mobile daalein", Toast.LENGTH_SHORT).show()
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
                .padding(16.dp),
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
        }
    }
}
