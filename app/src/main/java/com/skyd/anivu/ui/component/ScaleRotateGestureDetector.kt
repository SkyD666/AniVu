package com.skyd.anivu.ui.component

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class ScaleRotateGestureDetector(
    private val mListener: OnScaleRotateGestureListener
) {
    private var isZoom = false

    var useAnimate: Boolean = true

    var scale = 1f      // 伸缩比例
    var translationX = 0f // 移动X
    var translationY = 0f // 移动Y
    var rotation = 0f // 旋转角度

    var lastScale = 1f      // 上次松开触摸前的伸缩比例
    var lastTranslationX = 0f // 上次松开触摸前的移动X
    var lastTranslationY = 0f // 上次松开触摸前的移动Y
    var lastRotation = 0f // 上次松开触摸前的旋转角度

    // 移动过程中临时变量
    private var actionX = 0f
    private var actionY = 0f
    private var spacing = 0f
    private var degree = 0f
    private var moveType = 0 // 0=未选择，2=缩放

    // 恢复初始
    fun restore(targetView: View? = null) {
        val animators: Array<Animator>
        if (targetView == null) {
            animators = arrayOf(
                ValueAnimator.ofFloat(rotation, 0f),
                ValueAnimator.ofFloat(translationX, 0f),
                ValueAnimator.ofFloat(translationY, 0f),
                ValueAnimator.ofFloat(scale, 1f)
            )
        } else {
            animators = arrayOf(
                ObjectAnimator.ofFloat(targetView, View.ROTATION, rotation, 0f),
                ObjectAnimator.ofFloat(targetView, View.TRANSLATION_X, translationX, 0f),
                ObjectAnimator.ofFloat(targetView, View.TRANSLATION_Y, translationY, 0f),
                ObjectAnimator.ofFloat(targetView, View.SCALE_X, scale, 1f),
                ObjectAnimator.ofFloat(targetView, View.SCALE_Y, scale, 1f)
            )
        }

        AnimatorSet().apply {
            playTogether(*animators)
            duration = if (useAnimate) 260 else 0
            interpolator = DecelerateInterpolator()
            addListener(onEnd = {
                scale = 1f
                translationX = 0f
                translationY = 0f
                rotation = 0f
                lastScale = 1f
                lastTranslationX = 0f
                lastTranslationY = 0f
                lastRotation = 0f
                actionX = 0f
                actionY = 0f
                spacing = 0f
                degree = 0f
                moveType = 0
                mListener.onUpdate(this@ScaleRotateGestureDetector)
            })
            start()
        }

        isZoom = false
        mListener.isZoomChanged(false)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false

        if (event.pointerCount > 1) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
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
                    translationX = lastTranslationX + centerX - actionX
                    translationY = lastTranslationY + centerY - actionY
                    actionX = centerX
                    actionY = centerY

                    scale = lastScale * getSpacing(event) / spacing
                    rotation = lastRotation + getDegree(event) - degree
                    rotation = (rotation / abs(rotation)) * (abs(rotation) % 360)

                    if (!isZoom &&
                        (translationX != 0f || translationY != 0f || scale != 1f || rotation != 0f)
                    ) {
                        isZoom = true
                        mListener.isZoomChanged(true)
                    }

                    handled = mListener.onUpdate(this)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> if (event.pointerCount > 1) {
                    moveType = 0
                    lastScale = scale
                    lastTranslationX = translationX
                    lastTranslationY = translationY
                    lastRotation = rotation

                    handled = false
                }
            }
        }
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

    interface OnScaleRotateGestureListener {
        fun onUpdate(rotationDetector: ScaleRotateGestureDetector): Boolean = false

        fun isZoomChanged(isZoom: Boolean) {}
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}