package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.regex.Pattern

class RideAcceptService : AccessibilityService() {

    private var lastClickTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPkg = event?.packageName?.toString() ?: return
        if (eventPkg == applicationContext.packageName) return

        val prefs = PreferencesManager(applicationContext)
        if (!prefs.autoAccept) return

        val rootNode = rootInActiveWindow ?: return
        scanAndExecute(rootNode, prefs)
    }

    private fun scanAndExecute(rootNode: AccessibilityNodeInfo, prefs: PreferencesManager) {
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
            // Normal Pickup distance check
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
                val msg = if (prefs.isGoHomeEnabled) "🏠 Home Destination Accepted!" else "⚡ Order Auto-Accepted!"
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
