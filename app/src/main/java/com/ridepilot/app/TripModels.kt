package com.ridepilot.app

data class AcceptedTrip(
    val provider: String = "Ola",
    val fare: String = "₹120",
    val pickup: String = "Current Location",
    val drop: String = "Drop Location",
    val timestamp: Long = System.currentTimeMillis()
)
