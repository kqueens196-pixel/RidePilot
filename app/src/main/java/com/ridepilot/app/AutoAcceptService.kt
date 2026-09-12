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
        if (!isWithinDistance(rootInActiveWindow ?: return)) return
        if (!isRideAllowed(rootInActiveWindow)) return
        if (event == null || !prefs.autoAccept || !prefs.isPremiumActive() || prefs.isBlocked) return

        val pkgName = event.packageName?.toString() ?: ""
        val targetApps = listOf("rapido", "olacabs", "uber", "porter")
        val isTarget = targetApps.any { pkgName.lowercase().contains(it) }
        if (!isTarget) return

        val rootNode = rootInActiveWindow ?: return

        val now = System.currentTimeMillis()
        if (now - lastAcceptTime < 2500) return

        scanAndProcessOrder(rootNode, pkgName)
    }

    private fun scanAndProcessOrder(root: AccessibilityNodeInfo, pkg: String) {
        val allText = mutableListOf<String>()
        collectText(root, allText)
        val combinedText = allText.joinToString(" ")

        var fare = "₹85"
        val fareMatcher = Pattern.compile("₹\\s*([0-9]+)").matcher(combinedText)
        if (fareMatcher.find()) {
            fare = "₹" + fareMatcher.group(1)
        }

        var distanceKm = 1.5f
        val kmMatcher = Pattern.compile("([0-9]+(\\.[0-9]+)?)\\s*(km|KM)").matcher(combinedText)
        if (kmMatcher.find()) {
            distanceKm = kmMatcher.group(1)?.toFloatOrNull() ?: 1.5f
        }

        if (distanceKm > prefs.maxPickupKm) {
            return
        }

        val acceptKeywords = listOf("ACCEPT", "Accept", "ACCEPT ORDER", "SWIPE TO ACCEPT", "Accept Ride")
        // Anti-Detection: Natural human tap delay simulation (140ms - 320ms)
        try {
            val humanDelay = (140..320).random().toLong()
            Thread.sleep(humanDelay)
        } catch (_: Exception) {}

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
                trip = AcceptedTrip(
                    provider = providerName,
                    fare = fare,
                    pickup = "Pickup (~" + distanceKm + " KM)",
                    drop = "Customer Location"
                )
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

    private fun isWithinDistance(rootNode: android.view.accessibility.AccessibilityNodeInfo): Boolean {
        try {
            val texts = mutableListOf<String>()
            fun collect(node: android.view.accessibility.AccessibilityNodeInfo?) {
                if (node == null) return
                node.text?.let { texts.add(it.toString()) }
                for (i in 0 until node.childCount) {
                    collect(node.getChild(i))
                }
            }
            collect(rootNode)
            
            val maxKm = prefs.maxPickupDistance
            val kmRegex = Regex("([0-9]+(?:\.[0-9]+)?)\s*(?:km|kms)", RegexOption.IGNORE_CASE)
            val mRegex = Regex("([0-9]+)\s*(?:m|mtr|meter|meters)", RegexOption.IGNORE_CASE)

            for (t in texts) {
                kmRegex.find(t)?.let {
                    val km = it.groupValues[1].toFloatOrNull() ?: 0f
                    if (km > maxKm) return false
                }
                mRegex.find(t)?.let {
                    val meters = it.groupValues[1].toFloatOrNull() ?: 0f
                    val inKm = meters / 1000f
                    if (inKm > maxKm) return false
                }
            }
        } catch (_: Exception) {}
        return true
    }


    private fun isRideAllowed(rootNode: android.view.accessibility.AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false
        try {
            val texts = mutableListOf<String>()
            fun collect(node: android.view.accessibility.AccessibilityNodeInfo?) {
                if (node == null) return
                node.text?.let { if (it.isNotBlank()) texts.add(it.toString()) }
                for (i in 0 until node.childCount) {
                    collect(node.getChild(i))
                }
            }
            collect(rootNode)

            val maxPickup = prefs.maxPickupDistance
            val maxDrop = prefs.maxDropDistance

            val kmRegex = Regex("([0-9]+(?:\.[0-9]+)?)\\s*(?:km|kms)", RegexOption.IGNORE_CASE)
            val mRegex = Regex("([0-9]+)\\s*(?:m|mtr|meter|meters)", RegexOption.IGNORE_CASE)

            val detectedDistances = mutableListOf<Float>()

            for (t in texts) {
                kmRegex.findAll(t).forEach {
                    it.groupValues[1].toFloatOrNull()?.let { km -> detectedDistances.add(km) }
                }
                mRegex.findAll(t).forEach {
                    it.groupValues[1].toFloatOrNull()?.let { m -> detectedDistances.add(m / 1000f) }
                }
            }

            // If 2 distances appear (usually [Pickup, Drop]):
            // Smaller one is pickup, larger one is drop
            if (detectedDistances.size >= 2) {
                val sorted = detectedDistances.sorted()
                val pickup = sorted.first()
                val drop = sorted.last()
                if (pickup > maxPickup) return false
                if (drop > maxDrop) return false
            } else if (detectedDistances.size == 1) {
                // If only one distance is visible, treat as drop/trip limit
                if (detectedDistances[0] > maxDrop) return false
            }
        } catch (_: Exception) {}
        return true
    }

}