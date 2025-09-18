package com.soundwave.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.IntDef
import com.soundwave.lib.State.Companion.INIT
import com.soundwave.lib.State.Companion.IDLE
import com.soundwave.lib.State.Companion.DANCE
import com.soundwave.lib.State.Companion.STOP
import kotlin.math.pow
import kotlin.random.Random

class VolumeDanceInterpolator : Interpolator {

    override fun getInterpolation(f: Float): Float {
        val a = -2.8f
        val b = 3.8f
        return a * f.pow(2) + b * f
    }
}

@IntDef(INIT, IDLE, DANCE, STOP)
@Retention(AnnotationRetention.SOURCE)
annotation class State {
    companion object {
        const val INIT = -1
        const val IDLE = 0
        const val DANCE = 1
        const val STOP = 2
    }
}

open class SoundWaveView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    inner class VolumeBar {
        private var height: Int = 0
        private var halfWidth: Int = volumeBarHalfWidth
        private var danceStartTime: Long = 0
        private var danceStartDelay: Int = 0
        private var idleStartTime: Long = 0

        fun draw(
            canvas: Canvas?,
            paint: Paint,
            moveCanvas: Boolean,
            @State state: Int,
            index: Int
        ) {
            canvas ?: return
            // 非 idle 状态下需要重置 idleStartTime，这样才能在状态快速切换时让缓动重新开始
            if (state != IDLE) {
                idleStartTime = 0
            }
            when (state) {
                IDLE -> {
                    drawIdle(canvas, paint, moveCanvas, index)
                }
                DANCE -> {
                    drawDance(canvas, paint, moveCanvas, index)
                }
                INIT, STOP -> {
                    drawStop(canvas, paint, moveCanvas)
                }
            }
        }

        private fun drawStop(canvas: Canvas, paint: Paint, moveCanvas: Boolean) {
            // 停止
            if (moveCanvas) {
                canvas.translate((volumeBarMargin + 2 * volumeBarHalfWidth).toFloat(), 0f)
            }
            val rectF = RectF(
                -halfWidth.toFloat(), -minVolumeBarHeight.toFloat() / 2,
                halfWidth.toFloat(), minVolumeBarHeight.toFloat() / 2
            )
            canvas.drawRoundRect(
                rectF, halfWidth.toFloat(),
                halfWidth.toFloat(), paint
            )
        }

        private fun drawIdle(canvas: Canvas, paint: Paint, moveCanvas: Boolean, index: Int) {
            // 缓动
            val currentTime = System.currentTimeMillis()
            if (currentTime - idleStartTime > idleDuration) {
                // 开始一次新的 idle
                idleStartTime = currentTime
            }
            val idleStart = (1f * (currentTime - idleStartTime) / idleDuration * (volumeCount / 2 + volumeIdleCount + 4)).toInt()
            val mid = volumeCount / 2
            val x = if (index >= mid) {
                mid + idleStart - index
            } else {
                index - (mid - idleStart)
            }
            val drawHeight = if (x in 0 .. volumeIdleCount / 2) {
                dp2px(getIdleHeight(x)).toFloat()
            } else if (x in volumeIdleCount / 2 + 1 .. volumeIdleCount) {
                dp2px(getIdleHeight(volumeIdleCount - x)).toFloat()
            } else {
                minVolumeBarHeight.toFloat()
            }

            if (moveCanvas) {
                canvas.translate((volumeBarMargin + 2 * volumeBarHalfWidth).toFloat(), 0f)
            }
            val rectF = RectF(
                -halfWidth.toFloat(), -drawHeight / 2,
                halfWidth.toFloat(), drawHeight / 2
            )
            canvas.drawRoundRect(
                rectF, halfWidth.toFloat(),
                halfWidth.toFloat(), paint
            )
        }

        private fun drawDance(canvas: Canvas, paint: Paint, moveCanvas: Boolean, index: Int) {
            // 音柱跳动
            val currentTime = System.currentTimeMillis()
            if (currentTime - danceStartTime > danceDuration) {
                // 开始一次新的 dance
                danceStartDelay = random.nextInt(maxDanceDelay)
                danceStartTime = currentTime + danceStartDelay
                val volume = currentVolume.coerceAtLeast(minVolume).coerceAtMost(maxVolume)
                val rawHeight = 1f * (volume - minVolume) / (maxVolume - minVolume) * (maxVolumeBarHeight - minVolumeBarHeight) + minVolumeBarHeight
                val heightPortion = if (index in 0 until volumeHeightDistribution.size) {
                    volumeHeightDistribution[index]
                } else {
                    1f
                }
                height = (rawHeight * heightPortion).toInt()
            }
            val fraction = 1f * (currentTime - danceStartTime) / danceDuration
            val drawHeight = if (fraction in 0f..1f) {
                ((interpolator.getInterpolation(fraction) * height)
                    .coerceAtLeast(minVolumeBarHeight.toFloat()))
                    .coerceAtMost(maxVolumeBarHeight.toFloat())
            } else {
                minVolumeBarHeight.toFloat()
            }

            if (moveCanvas) {
                canvas.translate((volumeBarMargin + 2 * volumeBarHalfWidth).toFloat(), 0f)
            }
            val rectF = RectF(
                -halfWidth.toFloat(), -drawHeight / 2,
                halfWidth.toFloat(), drawHeight / 2
            )
            canvas.drawRoundRect(
                rectF, halfWidth.toFloat(),
                halfWidth.toFloat(), paint
            )
        }
    }

    var volumeCount = 37
    // 左右各16根音柱进入缓动模式
    var volumeIdleCount = 16
    var maxVolume = 35
    var minVolume = 4
    var danceDuration = 250
    var maxDanceDelay = 80
    var idleDuration = 3500

    var maxIdleHeight = dp2px(16f)
    var minVolumeBarHeight = dp2px(8f)
    var maxVolumeBarHeight = dp2px(36f)
    var volumeBarMargin = dp2px(3f)
    var volumeBarHalfWidth = dp2px(1.5f)
    private var volumeHeightDistribution = floatArrayOf(
        0.6f, 0.6f, 0.6f, 0.8f, 1f, 1.1f, 0.95f, 0.9f, 0.8f,
        0.75f, 0.8f, 0.9f, 0.95f, 1f, 1.1f, 1.2f, 1.5f, 1.4f, 1.3f,
        1.4f, 1.5f, 1.2f, 1.1f, 1f, 0.95f, 0.9f, 0.8f, 0.75f, 0.8f,
        0.9f, 0.95f, 1.1f, 1f, 0.8f, 0.6f, 0.6f, 0.6f
    )
    private var volumeBarColor = Color.parseColor("#A0000000")

    @State
    private var state = INIT
    private var volumeBarList = mutableListOf<VolumeBar>()
    private var currentVolume = -1
    private var interpolator: Interpolator = VolumeDanceInterpolator()
    private val random = Random(1)
    private val volumePaint = Paint()
    private var idleHeightGetter: (Int) -> Float = { x ->
        (x + 4).toFloat()
    }
    private var volumeChangeSmoother: (Int, Int) -> Int = { currentVolume, newVolume ->
        (currentVolume + newVolume) / 2
    }

    private var enableIdle = true

    private val animationRunnable: Runnable = object : Runnable {
        override fun run() {
            invalidate()
            post(this)
        }
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SoundWaveView)
            volumeCount = typedArray.getInt(R.styleable.SoundWaveView_volumeCount, 37)
            volumeIdleCount = typedArray.getInt(R.styleable.SoundWaveView_volumeIdleCount, 16)
            minVolume =  typedArray.getInt(R.styleable.SoundWaveView_minVolume, 4)
            maxVolume =  typedArray.getInt(R.styleable.SoundWaveView_maxVolume, 50)
            danceDuration = typedArray.getInt(R.styleable.SoundWaveView_danceDuration, 250)
            maxDanceDelay = typedArray.getInt(R.styleable.SoundWaveView_maxDanceDelay, 80)
            idleDuration = typedArray.getInt(R.styleable.SoundWaveView_idleDuration, 3500)
            maxIdleHeight = typedArray.getDimensionPixelSize(R.styleable.SoundWaveView_maxIdleHeight, dp2px(16f))
            minVolumeBarHeight = typedArray.getDimensionPixelSize(R.styleable.SoundWaveView_minVolumeBarHeight, dp2px(8f))
            maxVolumeBarHeight = typedArray.getDimensionPixelSize(R.styleable.SoundWaveView_maxVolumeBarHeight, dp2px(36f))
            volumeBarMargin = typedArray.getDimensionPixelSize(R.styleable.SoundWaveView_volumeBarMargin, dp2px(3f))
            volumeBarHalfWidth = typedArray.getDimensionPixelSize(R.styleable.SoundWaveView_volumeBarHalfWidth, dp2px(1.5f))
            volumeBarColor = typedArray.getColor(R.styleable.SoundWaveView_volumeBarColor, Color.parseColor("#11192D"))
            typedArray.recycle()
        }

        repeat(volumeCount) {
            volumeBarList.add(VolumeBar())
        }
        volumePaint.isAntiAlias = true
        volumePaint.color = volumeBarColor
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    fun handleVolume(newVolume: Int) {
        if (currentVolume < 0) {
            currentVolume = newVolume
            return
        }
        currentVolume = smoothVolumeChange(newVolume)
        state = if ((currentVolume < minVolume) && enableIdle) {
            IDLE
        } else {
            DANCE
        }
    }

    // 用来平滑变化音量
    private fun smoothVolumeChange(newVolume: Int): Int {
        return volumeChangeSmoother.invoke(currentVolume, newVolume)
    }

    private fun getIdleHeight(x: Int): Float {
        return idleHeightGetter.invoke(x)
    }

    /**
     * 设置音量平滑规则，避免当新音量来的时候，和当前音量差距过大导致跳跃过大
     * 一般用初始设置就行
     */
    fun setVolumeChangeSmoother(smoother: (Int, Int) -> Int) {
        this.volumeChangeSmoother = smoother
    }

    /**
     * 设置缓动的音柱高度变化函数，x是正在缓动的音柱的索引
     */
    fun setIdleHeightGetter(idleHeightGetter: (x: Int) -> Float) {
        this.idleHeightGetter = idleHeightGetter
    }

    /**
     * 设置音柱高度分布，floatArray 的长度应该等于 volumeCount
     */
    fun setDistribution(distribution: FloatArray) {
        volumeHeightDistribution = distribution
    }

    /**
     * 设置单个音柱的跳动长度变化插值器
     */
    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    /**
     * 是否启用音浪缓动
     */
    fun enableIdle(enable: Boolean) {
        enableIdle = enable
    }

    /**
     * 停止跳动音浪
     */
    fun stopDance() {
        state = STOP
    }

    /**
     * 设置音柱颜色
     */
    fun setColor(color: Int) {
        volumePaint.color = color
    }

    fun isStop(): Boolean {
        return state == STOP
    }

    fun getWaveAreaWidth(): Int {
        val size: Int = volumeBarList.size
        return size * 2 * volumeBarHalfWidth + (size - 1) * volumeBarMargin
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        var height = 0

        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> {
                specWidth
            }
            MeasureSpec.AT_MOST -> {
                val expectWidth = paddingStart + paddingEnd +
                        volumeCount * volumeBarHalfWidth * 2 +
                        volumeBarMargin * (volumeCount - 1)
                if (expectWidth < specWidth) {
                    expectWidth
                } else {
                    specWidth
                }
            }
            else -> {
                paddingStart + paddingEnd +
                volumeCount * volumeBarHalfWidth * 2 +
                volumeBarMargin * (volumeCount - 1)
            }
        }

        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> {
                specHeight
            }
            MeasureSpec.AT_MOST -> {
                val expectHeight = maxVolumeBarHeight + paddingTop + paddingBottom
                if (expectHeight < specHeight) {
                    expectHeight
                } else {
                    specHeight
                }
            }
            else -> {
                maxVolumeBarHeight + paddingTop + paddingBottom
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        if (volumeBarList.isEmpty()) {
            return
        }

        canvas.save()
        // 移动画布到中间位置
        canvas.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

        // 绘制中间的音量条
        val midPosition: Int = (volumeBarList.size - 1) / 2
        val midVolumeBar = volumeBarList[midPosition]
        midVolumeBar.draw(canvas, volumePaint, false, state, midPosition)

        canvas.save()
        // 绘制右侧音量条
        for (i in midPosition + 1 until volumeBarList.size) {
            val volumeBar = volumeBarList[i]
            volumeBar.draw(canvas, volumePaint, true, state, i)
        }

        canvas.restore()
        canvas.rotate(180f)
        // 绘制左侧音量条
        for (i in midPosition - 1 downTo 0) {
            val volumeBar = volumeBarList[i]
            volumeBar.draw(canvas, volumePaint, true, state, i)
        }
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(animationRunnable)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        when (visibility) {
            VISIBLE -> {
                state = INIT
                post(animationRunnable)
            }
            INVISIBLE, GONE -> {
                removeCallbacks(animationRunnable)
            }
        }
    }

}