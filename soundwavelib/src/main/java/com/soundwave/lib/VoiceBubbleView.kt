package com.soundwave.lib

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class VoiceBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val soundWaveView: SoundWaveView

    init {
        // 设置微信语音气泡背景
        setBackgroundResource(R.drawable.wechat_voice_bubble_background)
        
        // 设置内边距，为三角形留出空间
        setPadding(
            resources.getDimensionPixelSize(R.dimen.voice_bubble_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.voice_bubble_padding_vertical),
            resources.getDimensionPixelSize(R.dimen.voice_bubble_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.voice_bubble_padding_vertical_bottom)
        )
        
        // 创建并添加SoundWaveView
        soundWaveView = SoundWaveView(context, attrs)
        soundWaveView.setColor(android.graphics.Color.WHITE)
        addView(soundWaveView)
    }

    fun handleVolume(volume: Int) {
        soundWaveView.handleVolume(volume)
    }

    fun stopDance() {
        soundWaveView.stopDance()
    }

    fun enableIdle(enable: Boolean) {
        soundWaveView.enableIdle(enable)
    }

    fun setWaveColor(color: Int) {
        soundWaveView.setColor(color)
    }
}