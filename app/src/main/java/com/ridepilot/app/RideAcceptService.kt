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

class RideAcceptService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    private val targetKeywords = listOf(
        "ACCEPT", "ACCEPT RIDE", "ACCEPT ORDER", "SWIPE TO ACCEPT",
        "CONFIRM", "BOOK", "HAAN", "SWIPE"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPkg = event?.packageName?.toString() ?: return
        if (eventPkg == applicationContext.packageName) return // RidePilot ko kabhi click nahi karega

        val prefs = PreferencesManager(applicationContext)
        if (!prefs.autoAccept) return

        val recentOrderAlert = (System.currentTimeMillis() - OrderNotificationListener.lastOrderTimestamp) < 10000

        if (recentOrderAlert || isTargetDeliveryApp(eventPkg)) {
            triggerAcceptScanWithRetries(0)
        }
    }

    private fun isTargetDeliveryApp(pkg: String): Boolean {
        return pkg.contains("rapido", true) ||
               pkg.contains("porter", true) ||
               pkg.contains("shadowfax", true) ||
               pkg.contains("uberdriver", true) ||
               pkg.contains("swiggy", true) ||
               pkg.contains("zomato", true)
    }

    private fun triggerAcceptScanWithRetries(attempt: Int) {
        if (attempt > 6) return

        val rootNode = rootInActiveWindow
        if (rootNode != null && scanAndClick(rootNode)) {
            OrderNotificationListener.lastOrderTimestamp = 0
            return
        }

        handler.postDelayed({
            triggerAcceptScanWithRetries(attempt + 1)
        }, 500)
    }

    private fun scanAndClick(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.trim()?.uppercase() ?: ""
        val desc = node.contentDescription?.toString()?.trim()?.uppercase() ?: ""

        for (target in targetKeywords) {
            if (text == target || desc == target || text.startsWith("ACCEPT") || desc.startsWith("ACCEPT") || text.contains("SWIPE TO ACCEPT")) {
                if (performTapOrSwipe(node)) return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scanAndClick(child)) return true
        }
        return false
    }

    private fun performTapOrSwipe(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: "") + (node.contentDescription?.toString() ?: "")
        val rect = Rect()
        node.getBoundsInScreen(rect)

        if (text.uppercase().contains("SWIPE")) {
            if (rect.width() > 0 && rect.height() > 0) {
                swipeRight(rect.left.toFloat() + 50, rect.centerY().toFloat(), rect.right.toFloat() - 50, rect.centerY().toFloat())
                Toast.makeText(applicationContext, "⚡ Auto-Swiped Accept!", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Toast.makeText(applicationContext, "⚡ Order Auto-Accepted!", Toast.LENGTH_SHORT).show()
            return true
        }

        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Toast.makeText(applicationContext, "⚡ Order Auto-Accepted!", Toast.LENGTH_SHORT).show()
                return true
            }
            parent = parent.parent
        }

        if (rect.centerX() > 0 && rect.centerY() > 0) {
            tapCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
            Toast.makeText(applicationContext, "⚡ Screen Tap at Accept", Toast.LENGTH_SHORT).show()
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

    private fun swipeRight(fromX: Float, fromY: Float, toX: Float, toY: Float) {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
