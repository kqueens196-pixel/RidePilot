package com.ridepilot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PreferencesManager
    private lateinit var subManager: SubscriptionManager

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
    phone: String,
    onLogout: () -> Unit
) {
    var vehicle by remember { mutableStateOf(prefs.vehicle) }
    var radius by remember { mutableStateOf(prefs.radius) }
    var parcelMode by remember { mutableStateOf(prefs.parcelMode) }
    var autoAccept by remember { mutableStateOf(prefs.autoAccept) }

    val subStatus = subManager.status
    val daysLeft = subManager.daysRemaining
    val planName = subManager.planName

    AppScaffold(phone = phone, onLogout = onLogout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Subscription Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text(subStatus.name) }
                        )
                    }
                    Text(
                        text = "$daysLeft days remaining in current cycle",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))
            Text("Parcel Radius", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1 KM", "2 KM", "5 KM", "10 KM").forEach {
                    FilterChip(
                        selected = radius == it,
                        onClick = {
                            radius = it
                            prefs.radius = it
                        },
                        label = { Text(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SettingRow(
                title = "Parcel Mode",
                subtitle = if (subManager.canAccessParcelMatching()) "Allow parcel matching" else "Requires active subscription",
                checked = parcelMode,
                onCheckedChange = {
                    if (subManager.canAccessParcelMatching()) {
                        parcelMode = it
                        prefs.parcelMode = it
                    }
                }
            )
            SettingRow(
                title = "Auto-Accept",
                subtitle = if (subManager.canAccessAutoAccept()) "Only for authorized provider integrations" else "PRO feature only",
                checked = autoAccept,
                onCheckedChange = {
                    if (subManager.canAccessAutoAccept()) {
                        autoAccept = it
                        prefs.autoAccept = it
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Grant Required Permissions")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Connect Platforms")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Security: RidePilot never asks for third-party passwords or OTPs. Automatic acceptance requires an official provider API/partner authorization.",
                style = MaterialTheme.typography.bodySmall
            )
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
