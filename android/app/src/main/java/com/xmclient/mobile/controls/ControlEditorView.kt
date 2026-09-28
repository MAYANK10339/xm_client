package com.xmclient.mobile.controls

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ControlEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var controlLayout: ControlLayout = ControlLayout.createDefaultLayout()
        set(value) {
            field = value
            selectedButton = null
            invalidate()
        }

    var selectedButton: VirtualButton? = null
        private set

    var onButtonSelectedListener: ((VirtualButton?) -> Unit)? = null

    // Drawing Paints
    private val buttonBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#9918181B")
    }

    private val buttonStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#B3DC2626")
    }

    private val selectedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#EF4444")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.parseColor("#15FFFFFF")
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val rectF = RectF()
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var isDragging = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // 1. Draw subtle guide grid
        val step = 80f
        var x = step
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, gridPaint)
            x += step
        }
        var y = step
        while (y < h) {
            canvas.drawLine(0f, y, w, y, gridPaint)
            y += step
        }

        // 2. Draw all buttons
        val density = resources.displayMetrics.density
        for (btn in controlLayout.buttons) {
            val btnW = btn.width * density
            val btnH = btn.height * density
            val btnX = btn.x * w
            val btnY = btn.y * h

            rectF.set(btnX - btnW / 2, btnY - btnH / 2, btnX + btnW / 2, btnY + btnH / 2)

            buttonBgPaint.alpha = (btn.opacity * 255).toInt()
            val radius = if (btn.isRound) btnW / 2 else 12f * density

            canvas.drawRoundRect(rectF, radius, radius, buttonBgPaint)

            if (btn == selectedButton) {
                canvas.drawRoundRect(rectF, radius, radius, selectedStrokePaint)
            } else {
                canvas.drawRoundRect(rectF, radius, radius, buttonStrokePaint)
            }

            // Draw label
            val textY = rectF.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
            canvas.drawText(btn.label, rectF.centerX(), textY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                // Find clicked button (reverse order for top-most)
                var found: VirtualButton? = null
                for (btn in controlLayout.buttons.asReversed()) {
                    val btnW = btn.width * density
                    val btnH = btn.height * density
                    val btnX = btn.x * w
                    val btnY = btn.y * h

                    rectF.set(btnX - btnW / 2, btnY - btnH / 2, btnX + btnW / 2, btnY + btnH / 2)
                    if (rectF.contains(touchX, touchY)) {
                        found = btn
                        dragOffsetX = touchX - btnX
                        dragOffsetY = touchY - btnY
                        break
                    }
                }

                selectedButton = found
                isDragging = (found != null)
                onButtonSelectedListener?.invoke(found)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging && selectedButton != null) {
                    val btn = selectedButton!!
                    val newX = (event.x - dragOffsetX) / w
                    val newY = (event.y - dragOffsetY) / h

                    // Clamp to screen bounds
                    btn.x = newX.coerceIn(0.05f, 0.95f)
                    btn.y = newY.coerceIn(0.05f, 0.95f)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return super.onTouchEvent(event)
    }

    fun selectButton(btn: VirtualButton?) {
        selectedButton = btn
        onButtonSelectedListener?.invoke(btn)
        invalidate()
    }

    fun updateSelectedButtonSize(deltaDp: Float) {
        selectedButton?.let {
            it.width = (it.width + deltaDp).coerceIn(36f, 150f)
            it.height = (it.height + deltaDp).coerceIn(36f, 150f)
            invalidate()
        }
    }

    fun updateSelectedButtonOpacity(opacity: Float) {
        selectedButton?.let {
            it.opacity = opacity.coerceIn(0.1f, 1.0f)
            invalidate()
        }
    }
}
