package com.ridepilot.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingBubble(
            provider = intent?.getStringExtra("provider") ?: "Rapido",
            payout = intent?.getStringExtra("payout") ?: "₹180",
            pickup = intent?.getStringExtra("pickup") ?: "Hitech City"
        )
        return START_NOT_STICKY
    }

    private fun showFloatingBubble(provider: String, payout: String, pickup: String) {
        if (overlayView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        // Programmatic lightweight UI
        val card = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(0xFF1E1E1E.toInt())
            setPadding(36, 36, 36, 36)
        }

        val title = TextView(this).apply {
            text = "⚡ New Match: $provider ($payout)"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val details = TextView(this).apply {
            text = "Pickup: $pickup"
            textSize = 14f
            setTextColor(0xFFCCCCCC.toInt())
            setPadding(0, 8, 0, 16)
        }

        val btnRow = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }

        val acceptBtn = Button(this).apply {
            text = "Accept Order"
            setBackgroundColor(0xFF00C853.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener {
                Toast.makeText(applicationContext, "Order Accepted!", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        }

        val dismissBtn = Button(this).apply {
            text = "Dismiss"
            setBackgroundColor(0xFF424242.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { stopSelf() }
        }

        btnRow.addView(acceptBtn)
        btnRow.addView(dismissBtn)
        card.addView(title)
        card.addView(details)
        card.addView(btnRow)

        overlayView = card
        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }
}
