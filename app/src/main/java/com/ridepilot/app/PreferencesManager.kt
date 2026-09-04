package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var riderPhone: String
        get() = prefs.getString("rider_phone", "") ?: ""
        set(value) = prefs.edit().putString("rider_phone", value).apply()

    var vehicleType: String
        get() = prefs.getString("vehicle_type", "Bike") ?: "Bike"
        set(value) = prefs.edit().putString("vehicle_type", value).apply()

    var isRideEnabled: Boolean
        get() = prefs.getBoolean("is_ride_enabled", true)
        set(value) = prefs.edit().putBoolean("is_ride_enabled", value).apply()

    var isParcelEnabled: Boolean
        get() = prefs.getBoolean("is_parcel_enabled", true)
        set(value) = prefs.edit().putBoolean("is_parcel_enabled", value).apply()

    var isComboRouteEnabled: Boolean
        get() = prefs.getBoolean("combo_route_500m", true)
        set(value) = prefs.edit().putBoolean("combo_route_500m", value).apply()

    // 🏠 Go Home Mode
    var isGoHomeEnabled: Boolean
        get() = prefs.getBoolean("go_home_enabled", false)
        set(value) = prefs.edit().putBoolean("go_home_enabled", value).apply()

    var destinationAddress: String
        get() = prefs.getString("destination_address", "") ?: ""
        set(value) = prefs.edit().putString("destination_address", value).apply()

    var destinationRadiusKm: Float
        get() = prefs.getFloat("destination_radius_km", 1.0f)
        set(value) = prefs.edit().putFloat("destination_radius_km", value).apply()

    var maxPickupKm: Float
        get() = prefs.getFloat("max_pickup_km", 2.0f)
        set(value) = prefs.edit().putFloat("max_pickup_km", value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept", true)
        set(value) = prefs.edit().putBoolean("auto_accept", value).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
