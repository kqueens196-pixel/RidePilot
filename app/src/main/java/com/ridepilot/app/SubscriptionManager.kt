package com.ridepilot.app

import android.content.Context

enum class SubscriptionStatus {
    ACTIVE,
    TRIAL,
    EXPIRED
}

class SubscriptionManager(context: Context) {
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE
    val daysRemaining: Int = 28
    val planName: String = "Pro Fleet All-Access"

    fun canAccessAutoAccept(): Boolean = true
    fun canAccessParcelMatching(): Boolean = true
    fun canAccessMultiPlatform(): Boolean = true
}
