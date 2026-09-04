package com.ridepilot.app

data class Provider(
    val id: String,
    val name: String,
    val isConnected: Boolean = false,
    val supportsAutoAccept: Boolean = false
)

class ProviderManager {
    fun getAvailableProviders(): List<Provider> {
        return listOf(
            Provider("rapido", "Rapido Partner", isConnected = false, supportsAutoAccept = true),
            Provider("uber", "Uber Driver", isConnected = false, supportsAutoAccept = false),
            Provider("porter", "Porter Partner", isConnected = false, supportsAutoAccept = true),
            Provider("shadowfax", "Shadowfax Flash", isConnected = false, supportsAutoAccept = true)
        )
    }
}
