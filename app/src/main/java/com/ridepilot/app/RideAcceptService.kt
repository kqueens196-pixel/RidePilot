package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class RideAcceptService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return
        val prefs = PreferencesManager(applicationContext)

        // Agar rider ne settings me Auto-Accept on rakha hai
        if (prefs.autoAccept) {
            findAndClickAccept(rootNode)
        }
    }

    private fun findAndClickAccept(node: AccessibilityNodeInfo) {
        val keywords = listOf("ACCEPT", "ACCEPT ORDER", "SWIPE TO ACCEPT", "CONFIRM", "BOOK")

        // 1. Text-based node search & click
        for (kw in keywords) {
            val matchingNodes = node.findAccessibilityNodeInfosByText(kw)
            for (target in matchingNodes) {
                if (target.isClickable) {
                    target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    showToast("RidePilot: Order Auto-Accepted")
                    return
                } else if (target.parent?.isClickable == true) {
                    target.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    showToast("RidePilot: Order Auto-Accepted")
                    return
                }
            }
        }

        // Recursive tree traversal for child views
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findAndClickAccept(child)
            }
        }
    }

    // Direct Screen Coordinate Tap (Auto Tap via Gesture)
    fun performTapAt(x: Float, y: Float) {
        val swipePath = Path().apply {
            moveTo(x, y)
        }
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 50))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {}
}
