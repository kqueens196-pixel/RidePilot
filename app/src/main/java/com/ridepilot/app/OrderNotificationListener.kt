package com.ridepilot.app

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class OrderNotificationListener : NotificationListenerService() {

    companion object {
        @Volatile
        var lastIncomingPackage: String? = null
        @Volatile
        var lastOrderTimestamp: Long = 0
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnNonNull = sbn ?: return
        val packageName = sbnNonNull.packageName ?: return
        val notification = sbnNonNull.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val combinedText = "$title $text".uppercase()

        val isTargetApp = packageName.contains("rapido", true) ||
                          packageName.contains("porter", true) ||
                          packageName.contains("shadowfax", true) ||
                          packageName.contains("uberdriver", true) ||
                          packageName.contains("swiggy", true) ||
                          packageName.contains("zomato", true)

        val isOrderAlert = combinedText.contains("NEW ORDER") ||
                           combinedText.contains("RIDE REQUEST") ||
                           combinedText.contains("NEW RIDE") ||
                           combinedText.contains("PICKUP") ||
                           combinedText.contains("DUTY") ||
                           combinedText.contains("₹")

        if (isTargetApp && isOrderAlert) {
            val prefs = PreferencesManager(applicationContext)

            if (prefs.autoAccept) {
                var actionHandled = false
                notification.actions?.forEach { action ->
                    val actionTitle = action.title?.toString()?.uppercase() ?: ""
                    if (actionTitle.contains("ACCEPT") || actionTitle.contains("CONFIRM")) {
                        try {
                            action.actionIntent.send()
                            actionHandled = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (!actionHandled) {
                    try {
                        notification.contentIntent?.send()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                lastIncomingPackage = packageName
                lastOrderTimestamp = System.currentTimeMillis()
            }

            val intent = Intent(applicationContext, OverlayService::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("provider", if (packageName.contains("rapido", true)) "Rapido" else "Partner")
                putExtra("payout", if (text.contains("₹")) "₹" + text.substringAfter("₹").split(" ").first() else "New Order")
                putExtra("pickup", if (title.isNotEmpty()) title else "Incoming Request")
            }
            startService(intent)
        }
    }
}
