package com.ridepilot.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class RideAcceptService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
