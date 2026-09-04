package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.regex.Pattern

class RideAcceptService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastClickTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPkg = event?.packageName?.toString() ?: return
        if (eventPkg == applicationContext.packageName) return // RidePilot ko ignore karein

        val prefs = PreferencesManager(applicationContext)
        if (!prefs.autoAccept) return

        val rootNode = rootInActiveWindow ?: return

        // Scan any window: Fullscreen app ya Floating heads-up overlay (jaise Rapido popup)
        scanWindowForOrder(rootNode, prefs)
    }

    private fun scanWindowForOrder(rootNode: AccessibilityNodeInfo, prefs: PreferencesManager) {
        // Collect all text from current window/floating overlay
        val allTexts = mutableListOf<String>()
        val acceptButtons = mutableListOf<AccessibilityNodeInfo>()

        extractNodes(rootNode, allTexts, acceptButtons)

        if (acceptButtons.isEmpty()) return

        // Parse KM values from screen (e.g., "0.5 km", "2.2 km")
        var pickupKm = 0.0
        val kmPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*KM", Pattern.CASE_INSENSITIVE)

        for (str in allTexts) {
            val matcher = kmPattern.matcher(str)
            if (matcher.find()) {
                val value = matcher.group(1)?.toDoubleOrNull() ?: 0.0
                if (pickupKm == 0.0) {
                    pickupKm = value // First KM usually indicates pickup distance (0.5 km)
                }
            }
        }

        // KM Validation: Agar pickup distance user ke max limit se zyada hai to skip
        if (pickupKm > 0.0 && pickupKm > prefs.maxPickupKm) {
            return
        }

        // Throttling: prevent double tap in 1.5 seconds
        if (System.currentTimeMillis() - lastClickTime < 1500) return

        // Click the first valid Accept button found
        for (btn in acceptButtons) {
            if (clickTarget(btn)) {
                lastClickTime = System.currentTimeMillis()
                Toast.makeText(applicationContext, "⚡ Order Accepted (${pickupKm} KM)!", Toast.LENGTH_SHORT).show()
                break
            }
        }
    }

    private fun extractNodes(
        node: AccessibilityNodeInfo,
        texts: MutableList<String>,
        acceptButtons: MutableList<AccessibilityNodeInfo>
    ) {
        val text = node.text?.toString()?.trim() ?: ""
        val desc = node.contentDescription?.toString()?.trim() ?: ""

        if (text.isNotEmpty()) texts.add(text)
        if (desc.isNotEmpty()) texts.add(desc)

        val upperText = text.uppercase()
        val upperDesc = desc.uppercase()

        if (upperText == "ACCEPT" || upperDesc == "ACCEPT" ||
            upperText.contains("SWIPE TO ACCEPT") || upperText == "CONFIRM") {
            acceptButtons.add(node)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extractNodes(it, texts, acceptButtons) }
        }
    }

    private fun clickTarget(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            parent = parent.parent
        }

        // Tap screen bounds of the button directly
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.centerX() > 0 && rect.centerY() > 0) {
            tapCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
            return true
        }
        return false
    }

    private fun tapCoordinates(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
