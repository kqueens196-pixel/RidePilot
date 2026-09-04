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

    var isRideOn by remember { mutableStateOf(prefs.isRideEnabled) }
    var isParcelOn by remember { mutableStateOf(prefs.isParcelEnabled) }
    var isComboRouteOn by remember { mutableStateOf(prefs.isComboRouteEnabled) }
    var maxPickupKm by remember { mutableStateOf(prefs.maxPickupKm) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }

    var liveOrders by remember { mutableStateOf<List<NormalizedOrder>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoadingOrders = true
        val fetched = networkManager.fetchOrders()
        liveOrders = if (fetched.isNotEmpty()) {
            fetched
        } else {
            listOf(
                NormalizedOrder("ORD-101", "Rapido", OrderType.RIDE, "Lakdikapul Metro", "Necklace Road", 0.5, 24.0),
                NormalizedOrder("ORD-102", "Porter", OrderType.PARCEL, "Khairtabad", "Somajiguda", 1.2, 45.0),
                NormalizedOrder("ORD-103", "Rapido", OrderType.RIDE, "Ameerpet", "Madhapur", 4.5, 95.0)
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
            Toast.makeText(context, "Permissions active", Toast.LENGTH_SHORT).show()
        }
    }

    AppScaffold(phone = phone, onLogout = onLogout) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subscription Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Pro Fleet All-Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            AssistChip(onClick = { }, label = { Text("ACTIVE") })
                        }
                        Text(text = "Auto-Accept & Multi-App Sync Enabled", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Pickup Distance KM Control
            item {
                Text("Max Pickup Distance: ${"%.1f".format(maxPickupKm)} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Orders with pickup farther than this will be ignored", style = MaterialTheme.typography.bodySmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f).forEach { km ->
                        FilterChip(
                            selected = maxPickupKm == km,
                            onClick = {
                                maxPickupKm = km
                                prefs.maxPickupKm = km
                            },
                            label = { Text("${km} KM") }
                        )
                    }
                }
            }

            // Dedicated ON/OFF Switches
            item {
                SettingRow(
                    title = "⚡ Auto-Accept (Instant Tap)",
                    subtitle = "Instantly clicks Accept on Rapido, Porter, Uber & floating widgets",
                    checked = autoAccept,
                    onCheckedChange = {
                        autoAccept = it
                        prefs.autoAccept = it
                    }
                )

                SettingRow(
                    title = "🚖 Ride Orders Mode",
                    subtitle = "Turn ON / OFF accepting passenger bike & auto rides",
                    checked = isRideOn,
                    onCheckedChange = {
                        isRideOn = it
                        prefs.isRideEnabled = it
                    }
                )

                SettingRow(
                    title = "📦 Parcel Orders Mode",
                    subtitle = "Turn ON / OFF receiving delivery & courier parcel orders",
                    checked = isParcelOn,
                    onCheckedChange = {
                        isParcelOn = it
                        prefs.isParcelEnabled = it
                    }
                )

                if (isRideOn && isParcelOn) {
                    SettingRow(
                        title = "🛣️ Ride + Parcel Combo (Under 500m)",
                        subtitle = "Auto-accept parcel delivery if pickup falls within 500m of your ride",
                        checked = isComboRouteOn,
                        onCheckedChange = {
                            isComboRouteOn = it
                            prefs.isComboRouteEnabled = it
                        }
                    )
                }
            }

            // Permissions Shortcuts
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

            // Matched Live Orders Feed
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orders in Range (${matchedOrders.size})",
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
