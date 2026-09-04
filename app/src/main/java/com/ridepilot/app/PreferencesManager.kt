package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PHONE = "rider_phone"
        private const val KEY_VEHICLE = "selected_vehicle"
        private const val KEY_RADIUS = "parcel_radius"
        private const val KEY_PARCEL_MODE = "parcel_mode"
        private const val KEY_AUTO_ACCEPT = "auto_accept"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var riderPhone: String
        get() = prefs.getString(KEY_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    var vehicle: String
        get() = prefs.getString(KEY_VEHICLE, "Bike") ?: "Bike"
        set(value) = prefs.edit().putString(KEY_VEHICLE, value).apply()

    var radius: String
        get() = prefs.getString(KEY_RADIUS, "5 KM") ?: "5 KM"
        set(value) = prefs.edit().putString(KEY_RADIUS, value).apply()

    var parcelMode: Boolean
        get() = prefs.getBoolean(KEY_PARCEL_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_PARCEL_MODE, value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ACCEPT, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_ACCEPT, value).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_IS_LOGGED_IN).remove(KEY_PHONE).apply()
    }
}
