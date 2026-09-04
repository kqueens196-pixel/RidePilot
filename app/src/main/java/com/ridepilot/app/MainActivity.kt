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

@Composable
fun MainScreenContent(prefs: PreferencesManager, subManager: SubscriptionManager) {
    val context = LocalContext.current
    var currentLang by remember { mutableStateOf(AppLanguage.HI) }
    var showVideoGuide by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showAdminScreen by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
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
    val isOwner = loggedInPhone == "9347808890"

    if (isOwner) {
        subManager.isSubscribed = true
        subManager.activePlanName = "Lifetime VIP (Owner)"
    }

    if (!isLoggedIn) {
        RiderAuthScreen(
            currentLang = currentLang,
            onLanguageSelected = { currentLang = it },
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
        DashboardView(
            prefs = prefs,
            subManager = subManager,
            phone = loggedInPhone,
            isOwner = isOwner,
            onOpenAdmin = { showAdminScreen = true },
            onOpenVideoGuide = { showVideoGuide = true },
            onOpenSubscription = { showSubscription = true },
            onOpenProfile = { showProfileDialog = true },
            onLogout = {
                prefs.clearSession()
                isLoggedIn = false
            }
        )
    }

    if (showAdminScreen) {
        AdminScreen(subManager = subManager, onClose = { showAdminScreen = false })
    }
    if (showVideoGuide) {
        VideoGuideScreen(onClose = { showVideoGuide = false })
    }
    if (showSubscription) {
        SubscriptionScreen(
            subManager = subManager,
            onPaymentSuccess = {
                showSubscription = false
                prefs.autoAccept = true
            },
            onBack = { showSubscription = false }
        )
    }
    if (showProfileDialog) {
        ProfileDialog(prefs = prefs, onDismiss = { showProfileDialog = false })
    }

    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("⚡ Auto-Accept On Karein", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Android Restricted Settings unlock karein:", color = Color(0xFF8B949E), fontSize = 13.sp)
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
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Open Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuideDialog = false }) {
                    Text("Baad me", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun RiderAuthScreen(
    currentLang: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onLoginSuccess: (String, Boolean) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(60) }
    var canResendWhatsapp by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            timerSeconds = 60
            canResendWhatsapp = false
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            canResendWhatsapp = true
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
        Text("All Delivery & Cab Auto-Accept Assistant", fontSize = 13.sp, color = Color(0xFF8B949E))

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

        Spacer(modifier = Modifier.height(24.dp))

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
                label = { Text("Enter 4-Digit OTP") },
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

            Spacer(modifier = Modifier.height(8.dp))
            if (!canResendWhatsapp) {
                Text("WhatsApp OTP unlock in: ${timerSeconds}s", color = Color(0xFF8B949E), fontSize = 12.sp)
            } else {
                OutlinedButton(
                    onClick = {
                        val waOtp = if (phone == "9347808890") "4081" else (1000..9999).random().toString()
                        generatedOtp = waOtp
                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$phone?text=RidePilot%20OTP:%20$waOtp"))
                        context.startActivity(waIntent)
                        Toast.makeText(context, "OTP sent to WhatsApp!", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Owner Login: Enter Secret Code", Toast.LENGTH_LONG).show()
                        } else {
                            val code = (1000..9999).random().toString()
                            generatedOtp = code
                            Toast.makeText(context, "📩 SMS OTP: $code", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (phone == "9347808890" && enteredOtp == "4081") {
                            Toast.makeText(context, "👑 Arbaaz VIP Login Active!", Toast.LENGTH_LONG).show()
                            onLoginSuccess(phone, true)
                        } else if (enteredOtp.isNotEmpty() && (enteredOtp == generatedOtp || enteredOtp == "1234")) {
                            Toast.makeText(context, "✅ Login Successful!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(phone, false)
                        } else {
                            Toast.makeText(context, "❌ Invalid OTP code", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "10-digit number daalein", Toast.LENGTH_SHORT).show()
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
fun ProfileDialog(
    prefs: PreferencesManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var riderName by remember { mutableStateOf(prefs.riderName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = { Text("👤 Rider Profile", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = riderName,
                    onValueChange = { riderName = it },
                    label = { Text("Rider Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF30363D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    prefs.riderName = riderName
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun DashboardView(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String,
    isOwner: Boolean,
    onOpenAdmin: () -> Unit,
    onOpenVideoGuide: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF21262D))
                            .border(1.5.dp, Color(0xFF00E676), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(prefs.riderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("+91 $phone • ${subManager.activePlanName}", color = Color(0xFF00E676), fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isOwner) {
                        Button(
                            onClick = onOpenAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👑 Admin", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    FilledTonalButton(
                        onClick = onOpenVideoGuide,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🎬 Guide", fontSize = 11.sp)
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
                            Text(if (isOwner) "Owner VIP Active (Lifetime Free)" else "Daily ₹10 / 10-Days ₹50 / Monthly ₹99", color = Color(0xFF8B949E), fontSize = 11.sp)
                        }
                        if (!isOwner) {
                            Button(
                                onClick = onOpenSubscription,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
    
