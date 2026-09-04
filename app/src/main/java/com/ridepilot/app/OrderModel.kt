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
    val payoutInr: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class OrderMatchingEngine(
    private val prefs: PreferencesManager,
    private val subManager: SubscriptionManager
) {
    fun isOrderMatched(order: NormalizedOrder): Boolean {
        // 1. Subscription check
        if (order.type == OrderType.PARCEL && !subManager.canAccessParcelMatching()) {
            return false
        }

        // 2. Mode check
        if (order.type == OrderType.PARCEL && !prefs.parcelMode) {
            return false
        }

        // 3. Radius check
        val maxRadius = prefs.radius.replace(" KM", "").toDoubleOrNull() ?: 5.0
        if (order.distanceKm > maxRadius) {
            return false
        }

        return true
    }
}
