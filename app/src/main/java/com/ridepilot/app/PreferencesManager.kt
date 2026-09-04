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

    var vehicle: String
        get() = prefs.getString("vehicle", "Bike") ?: "Bike"
        set(value) = prefs.edit().putString("vehicle", value).apply()

    // Service Mode: "Both", "Only Ride", "Only Parcel"
    var serviceMode: String
        get() = prefs.getString("service_mode", "Both") ?: "Both"
        set(value) = prefs.edit().putString("service_mode", value).apply()

    // Dedicated switches
    var isParcelEnabled: Boolean
        get() = prefs.getBoolean("is_parcel_enabled", true)
        set(value) = prefs.edit().putBoolean("is_parcel_enabled", value).apply()

    var isComboRouteEnabled: Boolean
        get() = prefs.getBoolean("combo_route_500m", true)
        set(value) = prefs.edit().putBoolean("combo_route_500m", value).apply()

    // Distance Filters
    var maxPickupKm: Float
        get() = prefs.getFloat("max_pickup_km", 2.0f) // default 2 KM pickup limit
        set(value) = prefs.edit().putFloat("max_pickup_km", value).apply()

    var maxDropKm: Float
        get() = prefs.getFloat("max_drop_km", 10.0f)
        set(value) = prefs.edit().putFloat("max_drop_km", value).apply()

    var rideRadius: String
        get() = prefs.getString("ride_radius", "5 KM") ?: "5 KM"
        set(value) = prefs.edit().putString("ride_radius", value).apply()

    var parcelRadius: String
        get() = prefs.getString("parcel_radius", "5 KM") ?: "5 KM"
        set(value) = prefs.edit().putString("parcel_radius", value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept", true)
        set(value) = prefs.edit().putBoolean("auto_accept", value).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
