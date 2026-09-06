package com.ridepilot.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
                    color = Color(0xFF070B11)
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

    ModernDashboardView(
        prefs = prefs,
        subManager = subManager,
        phone = if (loggedInPhone.isEmpty()) "9347808890" else loggedInPhone,
        isOwner = true,
        onOpenGuide = { showGuide = true },
        onOpenPlans = { showPlans = true },
        onOpenAdmin = { showAdmin = true },
        onLogout = {
            prefs.clearSession()
            isLoggedIn = false
        }
    )

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

// trigger full poster flow update

// trigger full poster flow update
