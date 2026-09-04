package com.ridepilot.app

class OrderMatchingEngine(
    private val prefs: PreferencesManager,
    private val subManager: SubscriptionManager
) {
    fun isOrderMatched(order: NormalizedOrder): Boolean {
        // 1. Service Mode Filter
        val matchesMode = when (prefs.serviceMode) {
            "Only Ride" -> order.type == OrderType.RIDE
            "Only Parcel" -> order.type == OrderType.PARCEL
            else -> true // "Both"
        }
        if (!matchesMode) return false

        // 2. Distance KM Radius Filter
        val maxAllowedKm = if (order.type == OrderType.RIDE) {
            parseRadius(prefs.rideRadius)
        } else {
            parseRadius(prefs.parcelRadius)
        }

        return order.distanceKm <= maxAllowedKm
    }

    private fun parseRadius(radiusStr: String): Double {
        return radiusStr.replace(" KM", "").trim().toDoubleOrNull() ?: 10.0
    }
}
