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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    val providerManager = remember { ProviderManager() }

    var vehicle by remember { mutableStateOf(prefs.vehicle) }
    var serviceMode by remember { mutableStateOf(prefs.serviceMode) }
    var rideRadius by remember { mutableStateOf(prefs.rideRadius) }
    var parcelRadius by remember { mutableStateOf(prefs.parcelRadius) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }
    var showPlatformDialog by remember { mutableStateOf(false) }

    var liveOrders by remember { mutableStateOf<List<NormalizedOrder>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoadingOrders = true
        val fetched = networkManager.fetchOrders()
        liveOrders = if (fetched.isNotEmpty()) {
            fetched
        } else {
            listOf(
                NormalizedOrder("ORD-101", "Rapido", OrderType.RIDE, "MG Road", "Airport", 4.2, 320.0),
                NormalizedOrder("ORD-102", "Porter", OrderType.PARCEL, "Indiranagar", "Koramangala", 3.0, 150.0),
                NormalizedOrder("ORD-103", "Uber", OrderType.RIDE, "Hitech City", "Madhapur", 2.1, 140.0),
                NormalizedOrder("ORD-104", "Shadowfax", OrderType.PARCEL, "Gachibowli", "Kondapur", 7.5, 210.0)
            )
        }
        isLoadingOrders = false
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
            Toast.makeText(context, "Location & Notification granted", Toast.LENGTH_SHORT).show()
        }
    }

    AppScaffold(phone = phone, onLogout = onLogout) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = subManager.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            AssistChip(onClick = { }, label = { Text("ACTIVE PRO") })
                        }
                        Text(text = "${subManager.daysRemaining} days remaining • All features unlocked", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 1. Order Acceptance Mode: Both / Only Ride / Only Parcel
            item {
                Text("Order Type Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Both", "Only Ride", "Only Parcel").forEach { mode ->
                        FilterChip(
                            selected = serviceMode == mode,
                            onClick = {
                                serviceMode = mode
                                prefs.serviceMode = mode
                            },
                            label = { Text(mode) }
                        )
                    }
                }
            }

            // 2. Vehicle Selection
            item {
                Text("Vehicle Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Bike", "Auto", "Car", "Delivery").forEach {
                        FilterChip(
                            selected = vehicle == it,
                            onClick = {
                                vehicle = it
                                prefs.vehicle = it
                            },
                            label = { Text(it) }
                        )
                    }
                }
            }

            // 3. Ride KM Radius (Visible if Both or Only Ride)
            if (serviceMode != "Only Parcel") {
                item {
                    Text("Ride Max Radius (KM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("2 KM", "5 KM", "10 KM", "15 KM").forEach {
                            FilterChip(
                                selected = rideRadius == it,
                                onClick = {
                                    rideRadius = it
                                    prefs.rideRadius = it
                                },
                                label = { Text(it) }
                            )
                        }
                    }
                }
            }

            // 4. Parcel KM Radius (Visible if Both or Only Parcel)
            if (serviceMode != "Only Ride") {
                item {
                    Text("Parcel Max Radius (KM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1 KM", "3 KM", "5 KM", "10 KM").forEach {
                            FilterChip(
                                selected = parcelRadius == it,
                                onClick = {
                                    parcelRadius = it
                                    prefs.parcelRadius = it
                                },
                                label = { Text(it) }
                            )
                        }
                    }
                }
            }

            // 5. Auto-Accept Toggle (PRO Active)
            item {
                SettingRow(
                    title = "⚡ Auto-Accept (Instant Tap)",
                    subtitle = "Automatically taps accept on matching orders in background",
                    checked = autoAccept,
                    onCheckedChange = {
                        autoAccept = it
                        prefs.autoAccept = it
                    }
                )
            }

            // Actions Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val permissionsToRequest = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                            if (!permissionManager.hasOverlayPermission()) {
                                permissionManager.openOverlaySettings()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Permissions")
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accessibility")
                    }
                }
            }

            item {
                FilledTonalButton(
                    onClick = {
                        if (!permissionManager.hasOverlayPermission()) {
                            Toast.makeText(context, "Enable Overlay permission first", Toast.LENGTH_SHORT).show()
                            permissionManager.openOverlaySettings()
                        } else {
                            val intent = Intent(context, OverlayService::class.java).apply {
                                putExtra("provider", if (serviceMode == "Only Parcel") "Porter" else "Rapido")
                                putExtra("payout", "₹240")
                                putExtra("pickup", "Indiranagar 100ft Rd")
                            }
                            context.startService(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Floating Overlay Bubble")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matched Orders (${matchedOrders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoadingOrders) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
            }

            items(matchedOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(order.type.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("₹${order.payoutInr}", fontWeight = FontWeight.Bold)
                        }
                        Text("${order.pickupAddress} -> ${order.dropAddress}", style = MaterialTheme.typography.bodyMedium)
                        Text("${order.distanceKm} KM • Source: ${order.provider}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(phone: String, onLogout: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RidePilot ($phone)") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = checked,
                onClick = { onCheckedChange(!checked) },
                role = Role.Switch
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun RidePilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
