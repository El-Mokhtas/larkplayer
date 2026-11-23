package com.larkplayer.app.presentation.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.content.ContextCompat
import com.larkplayer.app.R

class VinylVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val vinylPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val groovePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val labelPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    private val needlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var artworkBitmap: Bitmap? = null
    private var vinylRotation = 0f
    private var targetRotation = 0f
    private var isSpinning = false
    private var rotationVelocity = 0f

    private val vinylRect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private var rotationAnimation: RotateAnimation? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (w.coerceAtMost(h) / 2f) * 0.85f

        vinylRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    fun setArtwork(bitmap: Bitmap?) {
        artworkBitmap = bitmap?.let { createCircularBitmap(it) }
        invalidate()
    }

    private fun createCircularBitmap(source: Bitmap): Bitmap {
        val size = (radius * 0.6).toInt()
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        val srcRect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(srcRect, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        val scaledBitmap = Bitmap.createScaledBitmap(source, size, size, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

        return output
    }

    fun startSpinning() {
        isSpinning = true
        rotationVelocity = 360f / 1800f // 1800ms per rotation = 33.3 RPM

        rotationAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1800
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
            fillAfter = true
        }
        startAnimation(rotationAnimation)
    }

    fun stopSpinning() {
        isSpinning = false
        clearAnimation()
        rotationAnimation = null
    }

    fun setRotationProgress(progress: Float) {
        // progress 0-1 for current track position
        vinylRotation = progress * 360f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw vinyl base (black)
        vinylPaint.color = ContextCompat.getColor(context, R.color.player_background)
        canvas.drawOval(vinylRect, vinylPaint)

        // Draw grooves
        groovePaint.color = ContextCompat.getColor(context, R.color.on_surface).copy(alpha = 0.1f)
        val grooveCount = 60
        for (i in 0 until grooveCount) {
            val grooveRadius = radius * (0.15f + 0.7f * i / grooveCount)
            canvas.drawCircle(centerX, centerY, grooveRadius, groovePaint)
        }

        // Draw label area
        vinylPaint.color = ContextCompat.getColor(context, R.color.surface_variant)
        canvas.drawCircle(centerX, centerY, radius * 0.28f, vinylPaint)

        // Draw center hole
        vinylPaint.color = ContextCompat.getColor(context, R.color.player_background)
        canvas.drawCircle(centerX, centerY, radius * 0.06f, vinylPaint)

        // Draw artwork in center
        artworkBitmap?.let { bitmap ->
            val bitmapX = centerX - bitmap.width / 2f
            val bitmapY = centerY - bitmap.height / 2f
            canvas.drawBitmap(bitmap, bitmapX, bitmapY, null)
        }

        // Draw needle (tonearm)
        drawNeedle(canvas)
    }

    private fun drawNeedle(canvas: Canvas) {
        val needleLength = radius * 1.2f
        val needleAngle = -45f // degrees from horizontal

        needlePaint.color = ContextCompat.getColor(context, R.color.on_surface)

        val needleX = centerX + radius * 0.9f * kotlin.math.cos(Math.toRadians(needleAngle))
        val needleY = centerY + radius * 0.9f * kotlin.math.sin(Math.toRadians(needleAngle))
        val tipX = centerX + needleLength * kotlin.math.cos(Math.toRadians(needleAngle))
        val tipY = centerY + needleLength * kotlin.math.sin(Math.toRadians(needleAngle))

        // Draw tonearm
        canvas.drawLine(needleX, needleY, tipX, tipY, needlePaint.apply { strokeWidth = 4f })

        // Draw needle head
        canvas.drawCircle(tipX, tipY, 8f, needlePaint)

        // Draw counterweight
        val cwAngle = needleAngle + 180f
        val cwX = centerX + radius * 0.3f * kotlin.math.cos(Math.toRadians(cwAngle))
        val cwY = centerY + radius * 0.3f * kotlin.math.sin(Math.toRadians(cwAngle))
        canvas.drawCircle(cwX, cwY, 16f, needlePaint.apply { color = ContextCompat.getColor(context, R.color.surface_variant) })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopSpinning()
    }
}