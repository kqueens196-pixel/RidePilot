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

    // Universal list of accept keywords across all delivery & cab apps
    private val universalAcceptKeywords = listOf(
        "ACCEPT", "ACCEPT ORDER", "SWIPE TO ACCEPT", "SLIDE TO ACCEPT",
        "CONFIRM", "BOOKING ACCEPT", "ACCEPT RIDE", "स्वीकारें",
        "రైడ్ అంగీకరించండి", "ஒப்புக்கொள்", "ಸ್ವೀಕರಿಸಿ", "स्वीकारा", "স্বীকার করুন"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val prefs = PreferencesManager(applicationContext)
        val subManager = SubscriptionManager(applicationContext)

        // Strict Subscription Check: Only work if active
        if (!prefs.autoAccept || !subManager.isSubscribed) return

        val windowList = windows
        if (!windowList.isNullOrEmpty()) {
            for (window in windowList) {
                val root = window.root ?: continue
                if (processUniversalNode(root, prefs)) return
            }
        }
        rootInActiveWindow?.let { processUniversalNode(it, prefs) }
    }

    private fun processUniversalNode(root: AccessibilityNodeInfo, prefs: PreferencesManager): Boolean {
        val allTexts = mutableListOf<String>()
        val targetNodes = mutableListOf<AccessibilityNodeInfo>()
        extractUniversal(root, allTexts, targetNodes)

        if (targetNodes.isEmpty()) return false

        var pickupText = "Nearby Location"
        var dropText = "Customer Drop Location"
        var fareText = "₹--"
        var pickupKm = 0.0

        for (i in allTexts.indices) {
            val t = allTexts[i].trim()
            if (t.contains("₹") && fareText == "₹--") fareText = t
            if (t.matches(Regex(".*\\d+(\\.\\d+)?\\s*km.*", RegexOption.IGNORE_CASE))) {
                val matcher = Pattern.compile("(\\d+(\\.\\d+)?)\\s*km", Pattern.CASE_INSENSITIVE).matcher(t)
                if (matcher.find()) {
                    val dist = matcher.group(1)?.toDoubleOrNull() ?: 0.0
                    if (pickupKm == 0.0) {
                        pickupKm = dist
                        if (i + 1 < allTexts.size && allTexts[i + 1].length > 4) pickupText = allTexts[i + 1]
                    } else {
                        if (i + 1 < allTexts.size && allTexts[i + 1].length > 4) dropText = allTexts[i + 1]
                    }
                }
            }
        }

        // Go Home Destination matching
        if (prefs.isGoHomeEnabled) {
            val target = prefs.destinationAddress.trim().uppercase()
            if (target.isNotEmpty()) {
                val fullText = allTexts.joinToString(" ").uppercase()
                val match = fullText.contains(target) || target.split(" ").any { it.length > 3 && fullText.contains(it) }
                if (!match) return false
            }
        } else {
            if (pickupKm > 0.0 && pickupKm > prefs.maxPickupKm) return false
        }

        if (System.currentTimeMillis() - lastClickTime < 1200) return false

        for (node in targetNodes) {
            if (executeUniversalAction(node)) {
                lastClickTime = System.currentTimeMillis()
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                prefs.addAcceptedTrip(
                    AcceptedTrip(
                        id = "TRIP-${System.currentTimeMillis() % 10000}",
                        provider = "Delivery / Cab Order",
                        pickup = pickupText,
                        drop = dropText,
                        fare = fareText,
                        time = time
                    )
                )
                Toast.makeText(applicationContext, "⚡ Order Auto-Accepted: $fareText", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    private fun extractUniversal(node: AccessibilityNodeInfo, texts: MutableList<String>, targets: MutableList<AccessibilityNodeInfo>) {
        val text = node.text?.toString()?.trim() ?: ""
        val desc = node.contentDescription?.toString()?.trim() ?: ""
        if (text.isNotEmpty()) texts.add(text)
        if (desc.isNotEmpty() && desc != text) texts.add(desc)

        val uText = text.uppercase()
        val uDesc = desc.uppercase()

        val isTarget = universalAcceptKeywords.any { kw ->
            uText == kw || uDesc == kw || uText.contains(kw) || uDesc.contains(kw)
        }

        if (isTarget) {
            targets.add(node)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extractUniversal(it, texts, targets) }
        }
    }

    private fun executeUniversalAction(node: AccessibilityNodeInfo): Boolean {
        val nodeText = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").uppercase()
        val rect = Rect()
        node.getBoundsInScreen(rect)

        // If swipe or slide order (like Uber/Swiggy/Porter swipe button)
        if (nodeText.contains("SWIPE") || nodeText.contains("SLIDE")) {
            if (rect.width() > 0 && rect.height() > 0) {
                val startX = rect.left + 50f
                val endX = rect.right - 50f
                val centerY = rect.centerY().toFloat()

                val path = Path().apply {
                    moveTo(startX, centerY)
                    lineTo(endX, centerY)
                }
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
                    .build()
                dispatchGesture(gesture, null, null)
                return true
            }
        }

        // Standard Click action
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        var p = node.parent
        while (p != null) {
            if (p.isClickable && p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            p = p.parent
        }

        // Fallback tap gesture on center of button
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
