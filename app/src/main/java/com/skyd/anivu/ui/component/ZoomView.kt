package com.skyd.anivu.ui.component

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import java.io.Serializable
import kotlin.math.atan2
import kotlin.math.sqrt

class ZoomView : FrameLayout {
    interface OnZoomListener {
        fun onIsZoomChanged(isZoom: Boolean)
    }

    private var onZoomListener: OnZoomListener? = null

    var isZoom = false
        private set

    var useAnimate: Boolean = true

    var scale = 1f      // 伸缩比例
    var mTranslationX = 0f // 移动X
    var mTranslationY = 0f // 移动Y
    var mRotation = 0f // 旋转角度

    // 移动过程中临时变量
    private var actionX = 0f
    private var actionY = 0f
    private var spacing = 0f
    private var degree = 0f
    private var moveType = 0 // 0=未选择，2=缩放


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    fun setOnZoomListener(listener: OnZoomListener) {
        onZoomListener = listener
    }

    // 恢复初始
    fun restore() {
        scale = 1f
        mTranslationX = 0f
        mTranslationY = 0f
        mRotation = 0f
        actionX = 0f
        actionY = 0f
        spacing = 0f
        degree = 0f
        moveType = 0

        val rotation = ObjectAnimator.ofFloat(this, "rotation", rotation, 0f)
        val translationX = ObjectAnimator.ofFloat(this, "translationX", translationX, 0f)
        val translationY = ObjectAnimator.ofFloat(this, "translationY", translationY, 0f)
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", scaleX, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", scaleY, 1f)

        AnimatorSet().apply {
            playTogether(rotation, translationX, translationY, scaleX, scaleY)
            duration = if (useAnimate) 260 else 0
            interpolator = DecelerateInterpolator()
            start()
        }

        isZoom = false
        onZoomListener?.onIsZoomChanged(false)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.onInterceptTouchEvent(ev)
    }

    // Accessibility services should not zoom
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = true

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount > 1) {
                moveType = 1
                val centerX = getCenterX(event)
                val centerY = getCenterY(event)
                actionX = centerX
                actionY = centerY
                spacing = getSpacing(event)
                degree = getDegree(event)

                handled = false
            }

            MotionEvent.ACTION_MOVE -> if (moveType == 1 && event.pointerCount > 1) {
                val centerX = getCenterX(event)
                val centerY = getCenterY(event)
                mTranslationX = mTranslationX + centerX - actionX
                mTranslationY = mTranslationY + centerY - actionY
                translationX = mTranslationX
                translationY = mTranslationY
                actionX = centerX
                actionY = centerY

                scale = scale * getSpacing(event) / spacing
                scaleX = scale
                scaleY = scale
                mRotation = mRotation + getDegree(event) - degree
                if (mRotation > 360) {
                    mRotation -= 360
                }
                if (mRotation < -360) {
                    mRotation += 360
                }
                rotation = mRotation

                if (!isZoom &&
                    (mTranslationX != 0f || mTranslationY != 0f || scale != 1f || mRotation != 0f)
                ) {
                    isZoom = true
                    onZoomListener?.onIsZoomChanged(true)
                }

                handled = false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> if (event.pointerCount > 1) {
                moveType = 0

                handled = false
            }
        }
        super.onTouchEvent(event)
        return handled
    }

    // 触碰两点间中心点X
    private fun getCenterX(event: MotionEvent): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && event.pointerCount > 1)
        // 安卓10及以上才有public float getRawX(int pointerIndex)方法
            (event.getRawX(0) + event.getRawX(1)) / 2f
        else event.rawX
    }

    // 触碰两点间中心点Y
    private fun getCenterY(event: MotionEvent): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && event.pointerCount > 1)
        // 安卓10及以上才有public float getRawX(int pointerIndex)方法
            (event.getRawY(0) + event.getRawY(1)) / 2f
        else event.rawY
    }

    // 触碰两点间距离
    private fun getSpacing(event: MotionEvent): Float {
        if (event.pointerCount <= 1) return 0f
        // 通过三角函数得到两点间的距离
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    // 取旋转角度
    private fun getDegree(event: MotionEvent): Float {
        if (event.pointerCount <= 1) return 0f
        // 得到两个手指间的旋转角度
        val deltaX = event.getX(0) - event.getX(1).toDouble()
        val deltaY = event.getY(0) - event.getY(1).toDouble()
        val radians = atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    init {
        isClickable = true
    }

    data class ZoomData(
        val useAnimate: Boolean,
        val scale: Float,
        val mTranslationX: Float,
        val mTranslationY: Float,
        val mRotation: Float,
    ) : Serializable
}

fun ZoomView.getData(): ZoomView.ZoomData {
    return ZoomView.ZoomData(
        useAnimate = useAnimate,
        scale = scale,
        mTranslationX = mTranslationX,
        mTranslationY = mTranslationY,
        mRotation = mRotation,
    )
}

fun ZoomView.setData(other: ZoomView.ZoomData?) {
    other ?: return
    useAnimate = other.useAnimate
    scale = other.scale
    mTranslationX = other.mTranslationX
    mTranslationY = other.mTranslationY
    mRotation = other.mRotation
    rotation = mRotation
    translationX = mTranslationX
    translationY = mTranslationY
    scaleX = scale
    scaleY = scale
}