package com.ridepilot.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
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
            val showGuide = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { showGuide.value = true }
            androidx.compose.runtime.LaunchedEffect(Unit) { permLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.POST_NOTIFICATIONS)) }
            RidePilotTheme {
                var isLoggedIn by remember { mutableStateOf(prefs.isLoggedIn) }
                var loggedInPhone by remember { mutableStateOf(prefs.riderPhone) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { phone ->
                        prefs.isLoggedIn = true
                        prefs.riderPhone = phone
                        loggedInPhone = phone
                        isLoggedIn = true
                    })
                } else {
                    MainDashboard(
                        prefs = prefs,
                        subManager = subManager,
                        phone = loggedInPhone,
                        onLogout = {
                            prefs.clearSession()
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    phone: String, onLogout: () -> Unit
) {
    val context = LocalContext.current

    var selectedVehicle by remember { mutableStateOf(prefs.vehicleType) }
    var isRideOn by remember { mutableStateOf(prefs.isRideEnabled) }
    var isParcelOn by remember { mutableStateOf(prefs.isParcelEnabled) }
    var isComboRouteOn by remember { mutableStateOf(prefs.isComboRouteEnabled) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }

    // Go Home state
    var isGoHomeOn by remember { mutableStateOf(prefs.isGoHomeEnabled) }
    var destText by remember { mutableStateOf(prefs.destinationAddress) }
    var destRadiusKm by remember { mutableStateOf(prefs.destinationRadiusKm) }
    var maxPickupKm by remember { mutableStateOf(prefs.maxPickupKm) }

    val waNum = "9347808890"
    val vehicleCategories = listOf(
        "Bike" to "🏍️",
        "Auto" to "🛺",
        "Cab/Car" to "🚕",
        "Mini Truck" to "🚚",
        "Heavy Pickup" to "🚛"
    )

    val destRadiusOptions = listOf(1.0f, 2.0f, 3.0f, 5.0f, 8.0f, 10.0f)

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("RidePilot Pro", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Fleet Master • $phone", color = Color(0xFF8B949E), fontSize = 12.sp)
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
            // Vehicle Fleet Selection
            item {
                Text("SELECT YOUR VEHICLE", color = Color(0xFF8B949E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    vehicleCategories.forEach { (name, icon) ->
                        val isSel = selectedVehicle == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0xFF1B382B) else Color(0xFF161B22))
                                .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedVehicle = name
                                    prefs.vehicleType = name
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, color = if (isSel) Color(0xFF00E676) else Color(0xFF8B949E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 🏠 GO HOME / DESTINATION CARD
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGoHomeOn) Color(0xFF1F2B1D) else Color(0xFF161B22)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, if (isGoHomeOn) Color(0xFFFFD600) else Color(0xFF30363D), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏠", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Go Home / Destination Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Accepts ONLY orders heading towards target", color = Color(0xFF8B949E), fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = isGoHomeOn,
                                onCheckedChange = {
                                    isGoHomeOn = it
                                    prefs.isGoHomeEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFFD600), checkedThumbColor = Color.Black)
                            )
                        }

                        if (isGoHomeOn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = destText,
                                onValueChange = {
                                    destText = it
                                    prefs.destinationAddress = it
                                },
                                label = { Text("Set Target Area / Home Location", color = Color(0xFF8B949E)) },
                                placeholder = { Text("e.g. Ameerpet, Kukatpally, Airport", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD600),
                                    unfocusedBorderColor = Color(0xFF30363D)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("TARGET DROP RADIUS (Under ${destRadiusKm.toInt()} KM)", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // 1 KM to 10 KM Selector Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                destRadiusOptions.forEach { r ->
                                    val isSelected = destRadiusKm == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFFFD600) else Color(0xFF263228))
                                            .border(1.dp, if (isSelected) Color(0xFFFFD600) else Color(0xFF3E4F3F), RoundedCornerShape(8.dp))
                                            .clickable {
                                                destRadiusKm = r
                                                prefs.destinationRadiusKm = r
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${r.toInt()} KM",
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("⚡ Pickup distance is UNLIMITED • Drop must be under ${destRadiusKm.toInt()} KM", color = Color(0xFF8B949E), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Modes & Auto-Accept Toggles
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ModernSettingRow("⚡ Auto-Accept (Instant Tap)", "Instant auto-click on Rapido / Porter orders", autoAccept, Color(0xFF00E676)) {
                            autoAccept = it
                            prefs.autoAccept = it
                        }
                        HorizontalDivider(color = Color(0xFF21262D))
                        ModernSettingRow("🚖 Ride Orders", "Passenger trips (Bike / Auto / Cab)", isRideOn, Color(0xFF00B0FF)) {
                            isRideOn = it
                            prefs.isRideEnabled = it
                        }
                        HorizontalDivider(color = Color(0xFF21262D))
                        ModernSettingRow("📦 Parcel Orders", "Deliveries / Parcels / Truck Cargo", isParcelOn, Color(0xFFFFAB00)) {
                            isParcelOn = it
                            prefs.isParcelEnabled = it
                        }
                        if (isRideOn && isParcelOn) {
                            HorizontalDivider(color = Color(0xFF21262D))
                            ModernSettingRow("🛣️ Combo Route (Under 500m)", "Match parcel along ongoing ride path", isComboRouteOn, Color(0xFF00E676)) {
                                isComboRouteOn = it
                                prefs.isComboRouteEnabled = it
                            }
                        }
                    }
                }
            }

            // Regular Pickup Limit (Disabled in Go Home mode)
            if (!isGoHomeOn) {
                item {
                    Text("MAX PICKUP DISTANCE", color = Color(0xFF8B949E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f).forEach { km ->
                            val isSel = maxPickupKm == km
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFF00E676) else Color(0xFF161B22))
                                    .border(1.dp, if (isSel) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(10.dp))
                                    .clickable {
                                        maxPickupKm = km
                                        prefs.maxPickupKm = km
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("${km} KM", color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 1-Click Permissions Center
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("EASY 1-CLICK PERMISSIONS", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    this@MainActivity.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Allow Restricted", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    this@MainActivity.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accessibility", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun ModernSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    activeColor: Color,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = Color(0xFF8B949E), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = Color(0xFF8B949E),
                uncheckedTrackColor = Color(0xFF21262D)
            )
        )
    }
}

@Composable
fun RidePilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF0D1117),
            surface = Color(0xFF161B22)
        ),
        content = content
    )
}
}
