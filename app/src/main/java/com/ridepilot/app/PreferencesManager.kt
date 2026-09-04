package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

data class AcceptedTrip(
    val provider: String,
    val fare: String,
    val pickup: String,
    val drop: String
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("RidePilotPrefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var riderPhone: String
        get() = prefs.getString("rider_phone", "") ?: ""
        set(value) = prefs.edit().putString("rider_phone", value).apply()

    var riderName: String
        get() = prefs.getString("rider_name", "Arbaaz (VIP)") ?: "Arbaaz (VIP)"
        set(value) = prefs.edit().putString("rider_name", value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept_enabled", false)
        set(value) = prefs.edit().putBoolean("auto_accept_enabled", value).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getAcceptedTrips(): List<AcceptedTrip> {
        return listOf(
            AcceptedTrip("Rapido", "₹65", "Ameerpet Metro", "Madhapur"),
            AcceptedTrip("Porter", "₹140", "Secunderabad", "Hitec City")
        )
    }
}
