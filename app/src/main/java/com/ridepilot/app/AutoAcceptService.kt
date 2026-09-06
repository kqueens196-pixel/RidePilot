package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.regex.Pattern

class AutoAcceptService : AccessibilityService() {

    private lateinit var prefs: PreferencesManager
    private var lastAcceptTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PreferencesManager(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !prefs.autoAccept) return

        val pkgName = event.packageName?.toString() ?: ""
        val targetApps = listOf("rapido", "olacabs", "uber", "porter")
        val isTarget = targetApps.any { pkgName.lowercase().contains(it) }
        if (!isTarget) return

        val rootNode = rootInActiveWindow ?: return

        // Throttle to avoid duplicate rapid clicks
        val now = System.currentTimeMillis()
        if (now - lastAcceptTime < 2500) return

        scanAndProcessOrder(rootNode, pkgName)
    }

    private fun scanAndProcessOrder(root: AccessibilityNodeInfo, pkg: String) {
        val allText = mutableListOf<String>()
        collectText(root, allText)
        val combinedText = allText.joinToString(" ")

        // Extract Fare
        var fare = "₹85"
        val fareMatcher = Pattern.compile("₹\\s*([0-9]+)").matcher(combinedText)
        if (fareMatcher.find()) {
            fare = "₹" + fareMatcher.group(1)
        }

        // Extract Distance in KM
        var distanceKm = 1.5f
        val kmMatcher = Pattern.compile("([0-9]+(\\.[0-9]+)?)\\s*(km|KM)").matcher(combinedText)
        if (kmMatcher.find()) {
            distanceKm = kmMatcher.group(1)?.toFloatOrNull() ?: 1.5f
        }

        // Filter Check: Skip if exceeds configured limit
        if (distanceKm > prefs.maxPickupKm) {
            return
        }

        // Find and Click Accept Button
        val acceptKeywords = listOf("ACCEPT", "Accept", "ACCEPT ORDER", "SWIPE TO ACCEPT", "Accept Ride")
        val clicked = findAndClick(root, acceptKeywords)

        if (clicked) {
            lastAcceptTime = System.currentTimeMillis()
            triggerAlert()

            val providerName = when {
                pkg.contains("rapido") -> "Rapido"
                pkg.contains("ola") -> "Ola"
                pkg.contains("uber") -> "Uber"
                pkg.contains("porter") -> "Porter"
                else -> "Ride"
            }

            prefs.addTripLog(
                provider = providerName,
                fare = fare,
                pickup = "Pickup (~${distanceKm} KM)",
                drop = "Customer Location"
            )
        }
    }

    private fun findAndClick(node: AccessibilityNodeInfo, keywords: List<String>): Boolean {
        val text = node.text?.toString()?.uppercase() ?: ""
        val desc = node.contentDescription?.toString()?.uppercase() ?: ""

        for (kw in keywords) {
            val upperKw = kw.uppercase()
            if (text.contains(upperKw) || desc.contains(upperKw)) {
                var target: AccessibilityNodeInfo? = node
                while (target != null) {
                    if (target.isClickable) {
                        target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                    target = target.parent
                }
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClick(child, keywords)) return true
        }
        return false
    }

    private fun collectText(node: AccessibilityNodeInfo, list: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { list.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { list.add(it) }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectText(it, list) }
        }
    }

    private fun triggerAlert() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
        } catch (_: Exception) {}

        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }

    override fun onInterrupt() {}
}
