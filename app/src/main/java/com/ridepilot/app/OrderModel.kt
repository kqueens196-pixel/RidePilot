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
        if (order.type == OrderType.RIDE && !prefs.isRideEnabled) return false
        if (order.type == OrderType.PARCEL && !prefs.isParcelEnabled) return false

        // 🏠 GO HOME / DESTINATION MODE
        if (prefs.isGoHomeEnabled) {
            val destTarget = prefs.destinationAddress.trim().uppercase()
            val orderDrop = order.dropAddress.trim().uppercase()

            if (destTarget.isEmpty()) return true

            val matchesDestination = orderDrop.contains(destTarget) || 
                                     destTarget.split(" ").any { word -> word.length > 3 && orderDrop.contains(word) }
            
            // Destination match hone par pickup unlimited allowed hai
            return matchesDestination
        }

        return order.distanceKm <= prefs.maxPickupKm
    }
}
