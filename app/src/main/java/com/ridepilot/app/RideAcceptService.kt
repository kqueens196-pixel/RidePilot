package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class RideAcceptService : AccessibilityService() {

    private var lastClickTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPkg = event?.packageName?.toString() ?: return
        if (eventPkg == applicationContext.packageName) return

        val prefs = PreferencesManager(applicationContext)
        if (!prefs.autoAccept) return

        val rootNode = rootInActiveWindow ?: return
        scanAndExecute(rootNode, prefs, eventPkg)
    }

    private fun scanAndExecute(rootNode: AccessibilityNodeInfo, prefs: PreferencesManager, pkg: String) {
        val allTexts = mutableListOf<String>()
        val acceptButtons = mutableListOf<AccessibilityNodeInfo>()

        extractAll(rootNode, allTexts, acceptButtons)
        if (acceptButtons.isEmpty()) return

        val fullScreenText = allTexts.joinToString(" ").uppercase()

        // 🏠 Go Home Mode Filter
        if (prefs.isGoHomeEnabled) {
            val target = prefs.destinationAddress.trim().uppercase()
            if (target.isNotEmpty()) {
                val matchesDrop = fullScreenText.contains(target) ||
                                  target.split(" ").any { it.length > 3 && fullScreenText.contains(it) }
                if (!matchesDrop) return
            }
        } else {
            var pickupKm = 0.0
            val kmPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*KM", Pattern.CASE_INSENSITIVE)
            for (str in allTexts) {
                val m = kmPattern.matcher(str)
                if (m.find()) {
                    val value = m.group(1)?.toDoubleOrNull() ?: 0.0
                    if (pickupKm == 0.0) pickupKm = value
                }
            }
            if (pickupKm > 0.0 && pickupKm > prefs.maxPickupKm) return
        }

        if (System.currentTimeMillis() - lastClickTime < 1500) return

        for (btn in acceptButtons) {
            if (clickTarget(btn)) {
                lastClickTime = System.currentTimeMillis()

                // Extract details from screen
                val fare = allTexts.find { it.contains("₹") } ?: "₹--"
                val addresses = allTexts.filter { it.length > 8 && !it.contains("ACCEPT", true) && !it.contains("₹") }
                val pickup = if (addresses.isNotEmpty()) addresses[0] else "Current Location"
                val drop = if (addresses.size > 1) addresses[1] else "Destination Location"

                val provider = when {
                    pkg.contains("rapido", true) -> "Rapido"
                    pkg.contains("porter", true) -> "Porter"
                    pkg.contains("uber", true) -> "Uber"
                    pkg.contains("shadowfax", true) -> "Shadowfax"
                    else -> "Partner Order"
                }

                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                // 💾 Save to Recent Trips
                prefs.addAcceptedTrip(
                    AcceptedTrip(
                        id = "TRIP-${System.currentTimeMillis() % 10000}",
                        provider = provider,
                        pickup = pickup,
                        drop = drop,
                        fare = fare,
                        time = time
                    )
                )

                val msg = if (prefs.isGoHomeEnabled) "🏠 Home Route Accepted!" else "⚡ Order Auto-Accepted ($fare)!"
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                break
            }
        }
    }

    private fun extractAll(
        node: AccessibilityNodeInfo,
        texts: MutableList<String>,
        buttons: MutableList<AccessibilityNodeInfo>
    ) {
        val text = node.text?.toString()?.trim() ?: ""
        val desc = node.contentDescription?.toString()?.trim() ?: ""

        if (text.isNotEmpty()) texts.add(text)
        if (desc.isNotEmpty()) texts.add(desc)

        val uText = text.uppercase()
        val uDesc = desc.uppercase()

        if (uText == "ACCEPT" || uDesc == "ACCEPT" || uText.contains("SWIPE TO ACCEPT") || uText == "CONFIRM") {
            buttons.add(node)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extractAll(it, texts, buttons) }
        }
    }

    private fun clickTarget(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        var p = node.parent
        while (p != null) {
            if (p.isClickable) return p.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            p = p.parent
        }

        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.centerX() > 0 && rect.centerY() > 0) {
            val path = Path().apply { moveTo(rect.centerX().toFloat(), rect.centerY().toFloat()) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
                .build()
            dispatchGesture(gesture, null, null)
            return true
        }
        return false
    }

    override fun onInterrupt() {}
}
