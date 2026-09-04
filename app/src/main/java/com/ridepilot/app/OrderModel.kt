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
        // 1. Direct Toggle Check
        if (order.type == OrderType.RIDE && !prefs.isRideEnabled) return false
        if (order.type == OrderType.PARCEL && !prefs.isParcelEnabled) return false

        // 2. Max KM Filter
        return order.distanceKm <= prefs.maxPickupKm + 5.0
    }
}
