package com.larkplayer.app.presentation.ui.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.larkplayer.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Random

class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val wavePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var barHeights: FloatArray = FloatArray(0)
    private var targetHeights: FloatArray = FloatArray(0)
    private var barCount = 0
    private var barWidth = 0f
    private var spacing = 0f

    private val colors = intArrayOf(
        R.color.visualizer_color_1,
        R.color.visualizer_color_2,
        R.color.visualizer_color_3,
        R.color.visualizer_color_4
    )

    private var animationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private val random = Random()
    private var isAnimating = false

    init {
        setupVisualizer()
    }

    private fun setupVisualizer() {
        // Calculate bar count based on width
        // Will be recalculated in onSizeChanged
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        barWidth = resources.getDimension(R.dimen.visualizer_bar_width)
        spacing = resources.getDimension(R.dimen.visualizer_bar_spacing)
        barCount = ((w.toFloat() + spacing) / (barWidth + spacing)).toInt().coerceAtLeast(20)

        barHeights = FloatArray(barCount)
        targetHeights = FloatArray(barCount)
    }

    fun startVisualizer(audioSessionId: Int) {
        isAnimating = true
        // In real implementation, connect to Visualizer or AudioProcessor
        startAnimation()
    }

    fun stopVisualizer() {
        isAnimating = false
        animationJob?.cancel()
        animationJob = null
        // Reset heights
        for (i in barHeights.indices) {
            barHeights[i] = 0f
        }
        invalidate()
    }

    private fun startAnimation() {
        animationJob = scope.launch {
            while (isAnimating) {
                updateBars()
                invalidate()
                kotlinx.coroutines.delay(50) // ~20fps
            }
        }
    }

    private fun updateBars() {
        val maxHeight = height.toFloat() * 0.8f

        for (i in 0 until barCount) {
            // Smooth interpolation
            val target = if (isAnimating) {
                (random.nextFloat() * 0.7f + 0.1f) * maxHeight
            } else {
                0f
            }
            targetHeights[i] = target

            // Smooth animation
            val diff = targetHeights[i] - barHeights[i]
            barHeights[i] += diff * 0.3f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val startX = (width - (barCount * barWidth + (barCount - 1) * spacing)) / 2f

        for (i in 0 until barCount) {
            val x = startX + i * (barWidth + spacing)
            val barHeight = barHeights[i]
            val colorRes = colors[i % colors.size]
            val color = ContextCompat.getColor(context, colorRes)

            paint.color = color
            wavePaint.color = color

            // Draw bar (centered vertically)
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            val rect = RectF(x, top, x + barWidth, bottom)

            // Rounded corners
            val radius = barWidth / 2
            canvas.drawRoundRect(rect, radius, radius, paint)

            // Draw wave line on top
            canvas.drawLine(x, top, x + barWidth, top, wavePaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopVisualizer()
        scope.cancel()
    }
}