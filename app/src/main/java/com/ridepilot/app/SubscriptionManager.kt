package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

data class Plan(
    val id: String,
    val title: String,
    val price: String,
    val validityDays: Int,
    val tag: String = ""
)

class SubscriptionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_sub", Context.MODE_PRIVATE)

    val plans = listOf(
        Plan("daily", "Daily Pass", "₹10", 1, "Trial"),
        Plan("10days", "10 Days Plan", "₹50", 10, "Popular"),
        Plan("monthly", "Monthly Plan", "₹99", 30, "Best Value"),
        Plan("yearly", "Yearly VIP", "₹800", 365, "Max Savings")
    )

    var isSubscribed: Boolean
        get() = prefs.getBoolean("is_active", true)
        set(value) = prefs.edit().putBoolean("is_active", value).apply()

    var activePlanName: String
        get() = prefs.getString("plan_name", "Monthly Plan") ?: "Monthly Plan"
        set(value) = prefs.edit().putString("plan_name", value).apply()

    var expiryTimeMillis: Long
        get() = prefs.getLong("expiry_time", System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
        set(value) = prefs.edit().putLong("expiry_time", value).apply()
}
