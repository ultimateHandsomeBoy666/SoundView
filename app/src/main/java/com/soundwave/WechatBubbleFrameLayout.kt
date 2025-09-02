package com.soundwave

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout

class WechatBubbleFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubblePath = Path()

    // 气泡属性
    private val bubbleColor = Color.parseColor("#95D75B") // 微信绿色
    private val cornerRadius = dp2px(12f)
    private val triangleSize = dp2px(8f)
    private val paddingHorizontal = dp2px(16f)
    private val paddingVertical = dp2px(12f)
    private val paddingBottom = dp2px(20f) // 底部留出三角形空间

    init {
        // 设置画笔
        bubblePaint.color = bubbleColor
        bubblePaint.style = Paint.Style.FILL
        
        // 设置背景透明，让我们自己绘制
        setBackgroundColor(Color.TRANSPARENT)
        
        // 关闭硬件加速以支持自定义绘制
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        // 设置内边距
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingBottom)
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBubblePath()
    }

    private fun updateBubblePath() {
        if (width <= 0 || height <= 0) return
        
        bubblePath.reset()
        
        val rect = RectF(0f, 0f, width.toFloat(), height - triangleSize.toFloat())
        
        // 绘制圆角矩形主体
        bubblePath.addRoundRect(rect, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CW)
        
        // 添加右下角的三角形尖尖
        val triangleStartX = width * 0.8f // 三角形起始位置
        val triangleStartY = height - triangleSize.toFloat()
        val triangleEndX = triangleStartX + triangleSize * 0.8f
        val triangleEndY = height.toFloat()
        
        // 创建三角形路径
        val trianglePath = Path()
        trianglePath.moveTo(triangleStartX, triangleStartY)
        trianglePath.lineTo(triangleEndX, triangleEndY)
        trianglePath.lineTo(triangleStartX + triangleSize, triangleStartY)
        trianglePath.close()
        
        // 将三角形合并到主路径
        bubblePath.op(trianglePath, Path.Op.UNION)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDraw(canvas: Canvas) {
        // 绘制气泡背景
        canvas.drawPath(bubblePath, bubblePaint)
        
        // 调用父类方法绘制子视图
        super.onDraw(canvas)
    }
}