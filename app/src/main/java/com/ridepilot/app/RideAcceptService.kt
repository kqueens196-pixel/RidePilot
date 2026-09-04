package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class RideAcceptService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        val prefs = PreferencesManager(applicationContext)

        if (prefs.autoAccept) {
            scanAndClickTarget(rootNode)
        }
    }

    private fun scanAndClickTarget(node: AccessibilityNodeInfo): Boolean {
        val targets = listOf(
            "ACCEPT", "ACCEPT RIDE", "ACCEPT ORDER",
            "SWIPE TO ACCEPT", "CONFIRM", "BOOK", "HAAN", "SWIPE"
        )

        // 1. Match by Button text
        val text = node.text?.toString()?.uppercase() ?: ""
        val desc = node.contentDescription?.toString()?.uppercase() ?: ""

        for (target in targets) {
            if (text.contains(target) || desc.contains(target)) {
                if (performActionClick(node)) return true
            }
        }

        // 2. Child nodes recursive scan
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scanAndClickTarget(child)) return true
        }

        return false
    }

    private fun performActionClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Toast.makeText(applicationContext, "⚡ RidePilot Auto-Accepted!", Toast.LENGTH_SHORT).show()
            return true
        }
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Toast.makeText(applicationContext, "⚡ RidePilot Auto-Accepted!", Toast.LENGTH_SHORT).show()
                return true
            }
            parent = parent.parent
        }

        // Coordinate click fallback if clickable flag is false
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.centerX() > 0 && rect.centerY() > 0) {
            tapCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
            Toast.makeText(applicationContext, "⚡ Auto-Tap at button", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun tapCoordinates(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
