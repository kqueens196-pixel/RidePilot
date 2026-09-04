package com.ridepilot.app

import android.Manifest
import android.content.Intent
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
    private val networkManager = NetworkManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(applicationContext)
        subManager = SubscriptionManager(applicationContext)

        setContent {
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
                        networkManager = networkManager,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    prefs: PreferencesManager,
    subManager: SubscriptionManager,
    networkManager: NetworkManager,
    phone: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    val matchingEngine = remember { OrderMatchingEngine(prefs, subManager) }

    var isRideOn by remember { mutableStateOf(prefs.isRideEnabled) }
    var isParcelOn by remember { mutableStateOf(prefs.isParcelEnabled) }
    var isComboRouteOn by remember { mutableStateOf(prefs.isComboRouteEnabled) }
    var maxPickupKm by remember { mutableStateOf(prefs.maxPickupKm) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }

    var liveOrders by remember { mutableStateOf<List<NormalizedOrder>>(emptyList()) }

    LaunchedEffect(Unit) {
        val fetched = networkManager.fetchOrders()
        liveOrders = if (fetched.isNotEmpty()) {
            fetched
        } else {
            listOf(
                NormalizedOrder("ORD-101", "Rapido", OrderType.RIDE, "Lakdikapul Metro", "Necklace Road", 0.5, 24.0),
                NormalizedOrder("ORD-102", "Porter", OrderType.PARCEL, "Khairtabad Hub", "Somajiguda Circle", 1.2, 45.0),
                NormalizedOrder("ORD-103", "Rapido", OrderType.RIDE, "Ameerpet Metro", "Madhapur Cyber", 4.5, 95.0)
            )
        }
    }

    val matchedOrders = liveOrders.filter { matchingEngine.isOrderMatched(it) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else true

        if (fineLocationGranted && notifGranted) {
            Toast.makeText(context, "Permissions Active", Toast.LENGTH_SHORT).show()
        }
    }

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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("RidePilot", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("+91 $phone", color = Color(0xFF8B949E), fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onLogout) {
                    Text("Logout", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
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
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PILOT ENGINE", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (autoAccept) Color(0x2200E676) else Color(0x22FF5252))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (autoAccept) "ONLINE • AUTO-ACCEPT" else "STANDBY",
                                    color = if (autoAccept) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pro Fleet All-Access Active", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Instant notification intercept & auto-tap enabled", color = Color(0xFF8B949E), fontSize = 13.sp)
                    }
                }
            }

            item {
                Text("DISPATCH MODES", color = Color(0xFF8B949E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeTile(
                        icon = "🚖",
                        title = "Rides",
                        subtitle = "Bike / Auto",
                        enabled = isRideOn,
                        onToggle = {
                            isRideOn = !isRideOn
                            prefs.isRideEnabled = isRideOn
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeTile(
                        icon = "📦",
                        title = "Parcels",
                        subtitle = "Courier / Food",
                        enabled = isParcelOn,
                        onToggle = {
                            isParcelOn = !isParcelOn
                            prefs.isParcelEnabled = isParcelOn
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ModernSettingRow(
                            title = "⚡ Auto-Accept (Instant Click)",
                            subtitle = "Automatic tap on Rapido / Porter accept buttons",
                            checked = autoAccept,
                            activeColor = Color(0xFF00E676),
                            onChecked = {
                                autoAccept = it
                                prefs.autoAccept = it
                            }
                        )

                        if (isRideOn && isParcelOn) {
                            HorizontalDivider(color = Color(0xFF21262D))
                            ModernSettingRow(
                                title = "🛣️ Same Route Combo (500m)",
                                subtitle = "Accept parcel on your ongoing ride route",
                                checked = isComboRouteOn,
                                activeColor = Color(0xFF00B0FF),
                                onChecked = {
                                    isComboRouteOn = it
                                    prefs.isComboRouteEnabled = it
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text("MAX PICKUP DISTANCE", color = Color(0xFF8B949E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f).forEach { km ->
                        val isSelected = maxPickupKm == km
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00E676) else Color(0xFF161B22))
                                .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF30363D), RoundedCornerShape(10.dp))
                                .clickable {
                                    maxPickupKm = km
                                    prefs.maxPickupKm = km
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${km} KM",
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                            if (!permissionManager.hasOverlayPermission()) {
                                permissionManager.openOverlaySettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Permissions", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accessibility", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIVE RADAR ORDERS", color = Color(0xFF8B949E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${matchedOrders.size} Available", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(matchedOrders) { order ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${order.provider} • ${order.type.name}",
                                color = if (order.type == OrderType.RIDE) Color(0xFF00B0FF) else Color(0xFFFFAB00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text("₹${order.payoutInr.toInt()}", color = Color(0xFF00E676), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${order.pickupAddress} ➔ ${order.dropAddress}", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${order.distanceKm} KM Pickup Distance", color = Color(0xFF8B949E), fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ModeTile(
    icon: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0xFF16251E) else Color(0xFF161B22)
        ),
        modifier = modifier
            .border(
                1.5.dp,
                if (enabled) Color(0xFF00E676) else Color(0xFF30363D),
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 24.sp)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (enabled) Color(0xFF00E676) else Color(0xFF484F58))
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color(0xFF8B949E), fontSize = 12.sp)
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
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = Color(0xFF8B949E), fontSize = 12.sp)
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
