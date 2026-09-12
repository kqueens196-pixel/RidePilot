package com.ridepilot.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: TextView? = null
    private lateinit var prefs: PreferencesManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingBubble()
    }

    private fun updateBubbleState(isActive: Boolean) {
        bubbleView?.let { view ->
            view.text = if (isActive) "RP\nON" else "RP\nOFF"
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (isActive) Color.parseColor("#00E676") else Color.parseColor("#FF5252"))
                setStroke(4, Color.WHITE)
            }
            view.background = shape
        }
    }

    private fun createFloatingBubble() {
        val density = resources.displayMetrics.density
        val sizePx = (58 * density).toInt()

        val textView = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setLineSpacing(0f, 0.9f)
        }
        bubbleView = textView
        updateBubbleState(prefs.autoAccept)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 350
        }

        bubbleView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f
            private var isClick = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(bubbleView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            prefs.autoAccept = !prefs.autoAccept
                            updateBubbleState(prefs.autoAccept)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager?.addView(bubbleView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleView?.let { windowManager?.removeView(it) }
    }
}
