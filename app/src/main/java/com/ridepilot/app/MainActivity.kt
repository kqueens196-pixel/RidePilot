package com.ridepilot.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RidePilotTheme {
                RidePilotApp(
                    onRequestPermissions = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun RidePilotApp(onRequestPermissions: () -> Unit) {
    var mode by remember { mutableStateOf("BOTH") }
    var vehicle by remember { mutableStateOf("Bike") }
    var radius by remember { mutableStateOf("2 KM") }
    var parcelMode by remember { mutableStateOf(true) }
    var autoAccept by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("RidePilot", fontWeight = FontWeight.Bold)
                    Text("ONE APP. EVERY RIDE. MORE CONTROL.", style = MaterialTheme.typography.labelSmall)
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Launch Offer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("₹99/month for the first 90 days")
                    Text("First eligible ride/order FREE for eligible new users during launch.")
                    Text("Day 91 onward: ₹150/month renewal")
                    Text("Optional One-Day Pass: ₹10")
                }
            }

            Text("Order Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BOTH" to "Parcel + Ride", "RIDE" to "Only Ride", "PARCEL" to "Only Parcel").forEach { (key, label) ->
                    FilterChip(selected = mode == key, onClick = { mode = key }, label = { Text(label) })
                }
            }

            Text("Vehicle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Bike", "Auto", "Car", "Delivery").forEach {
                    FilterChip(selected = vehicle == it, onClick = { vehicle = it }, label = { Text(it) })
                }
            }

            Text("Parcel Radius", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1 KM", "2 KM", "5 KM", "10 KM").forEach {
                    FilterChip(selected = radius == it, onClick = { radius = it }, label = { Text(it) })
                }
            }

            SettingRow("Parcel Mode", "Allow parcel matching", parcelMode) { parcelMode = it }
            SettingRow("Auto-Accept", "Only for authorized provider integrations", autoAccept) { autoAccept = it }

            HorizontalDivider()

            Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) {
                Text("Grant Required Permissions")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect Platforms")
            }

            Text(
                "Security: RidePilot never asks for third-party passwords or OTPs. Automatic acceptance requires an official provider API/partner authorization.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
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
        Modifier
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
