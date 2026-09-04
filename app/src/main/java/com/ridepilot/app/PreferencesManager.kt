package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

data class AcceptedTrip(
    val id: String = System.currentTimeMillis().toString(),
    val provider: String = "Order",
    val fare: String = "₹0",
    val pickup: String = "",
    val drop: String = "",
    val time: Long = System.currentTimeMillis()
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
        get() = prefs.getBoolean("auto_accept_enabled", true)
        set(value) = prefs.edit().putBoolean("auto_accept_enabled", value).apply()

    var maxPickupKm: Float
        get() = prefs.getFloat("max_pickup_km", 5.0f)
        set(value) = prefs.edit().putFloat("max_pickup_km", value).apply()

    var isGoHomeEnabled: Boolean
        get() = prefs.getBoolean("is_go_home_enabled", false)
        set(value) = prefs.edit().putBoolean("is_go_home_enabled", value).apply()

    var destinationAddress: String
        get() = prefs.getString("destination_address", "") ?: ""
        set(value) = prefs.edit().putString("destination_address", value).apply()

    var isComboRouteEnabled: Boolean
        get() = prefs.getBoolean("is_combo_route_enabled", false)
        set(value) = prefs.edit().putBoolean("is_combo_route_enabled", value).apply()

    private val trips = mutableListOf<AcceptedTrip>()

    fun addAcceptedTrip(trip: AcceptedTrip) {
        trips.add(0, trip)
    }

    fun getAcceptedTrips(): List<AcceptedTrip> {
        return if (trips.isNotEmpty()) trips else listOf(
            AcceptedTrip(provider = "Rapido", fare = "₹65", pickup = "Ameerpet Metro", drop = "Madhapur"),
            AcceptedTrip(provider = "Porter", fare = "₹140", pickup = "Secunderabad", drop = "Hitec City")
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
