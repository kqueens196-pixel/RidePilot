package com.ridepilot.app

enum class OrderType {
    RIDE,
    PARCEL
}

data class NormalizedOrder(
    val id: String,
    val provider: String,
    val type: OrderType,
    val pickupAddress: String,
    val dropAddress: String,
    val distanceKm: Double,
    val payoutInr: Double
)

class OrderMatchingEngine(
    private val prefs: PreferencesManager,
    private val subManager: SubscriptionManager
) {
    fun isOrderMatched(order: NormalizedOrder): Boolean {
        // 1. Service Mode Filter (Both, Only Ride, Only Parcel)
        val matchesMode = when (prefs.serviceMode) {
            "Only Ride" -> order.type == OrderType.RIDE
            "Only Parcel" -> order.type == OrderType.PARCEL
            else -> true // Both
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
