package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

enum class SubscriptionStatus {
    TRIAL,
    ACTIVE,
    EXPIRED
}

class SubscriptionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_subscription", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STATUS = "sub_status"
        private const val KEY_PLAN_NAME = "sub_plan_name"
        private const val KEY_DAYS_LEFT = "sub_days_left"
    }

    var status: SubscriptionStatus
        get() {
            val name = prefs.getString(KEY_STATUS, SubscriptionStatus.TRIAL.name) ?: SubscriptionStatus.TRIAL.name
            return try {
                SubscriptionStatus.valueOf(name)
            } catch (e: Exception) {
                SubscriptionStatus.TRIAL
            }
        }
        set(value) = prefs.edit().putString(KEY_STATUS, value.name).apply()

    var planName: String
        get() = prefs.getString(KEY_PLAN_NAME, "Starter Trial") ?: "Starter Trial"
        set(value) = prefs.edit().putString(KEY_PLAN_NAME, value).apply()

    var daysRemaining: Int
        get() = prefs.getInt(KEY_DAYS_LEFT, 7)
        set(value) = prefs.edit().putInt(KEY_DAYS_LEFT, value).apply()

    fun canAccessAutoAccept(): Boolean {
        return status == SubscriptionStatus.ACTIVE
    }

    fun canAccessParcelMatching(): Boolean {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL
    }
}
