package com.skyd.anivu.ui.player

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


class PlayerGestureDetector(
    private val mListener: PlayerGestureListener
) {
    // ================================================= 双指操作的变量
    private var isZoom = false
    var useAnimate: Boolean = true

    var scale = 1f          // 伸缩比例
    var translationX = 0f   // 移动X
    var translationY = 0f   // 移动Y
    var rotation = 0f       // 旋转角度

    private var lastScale = 1f          // 上次松开触摸前的伸缩比例
    private var lastTranslationX = 0f   // 上次松开触摸前的移动X
    private var lastTranslationY = 0f   // 上次松开触摸前的移动Y
    private var lastRotation = 0f       // 上次松开触摸前的旋转角度

    // 移动过程中临时变量
    private var actionX = 0f
    private var actionY = 0f
    private var spacing = 0f
    private var degree = 0f
    private var doublePointer = 0
    // =================================================

    // ================================================= 单指移动的变量
    private var singleMoveDownX = 0f
    private var singleMoveDownY = 0f
    private var singleMove = 0      // 0：已抬起、1：按下、2：已移动并且移动距离超过阈值
    // =================================================

    // 恢复初始Zoom状态
    fun restoreZoom(targetView: View? = null) {
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
                doublePointer = 0
                mListener.onZoomUpdate(this@PlayerGestureDetector)
            })
            start()
        }

        isZoom = false
        mListener.isZoomChanged(false)
    }

    private var longPressed = false
    private val longPressHandler: Handler = Handler(Looper.getMainLooper())
    private var longPressedRunnable = Runnable {
        longPressed = true
        mListener.onLongPress()
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) {
                    singleMove = 1
//                    Log.e("TAG", "onTouchEvent: move down")
                    singleMoveDownX = x
                    singleMoveDownY = y

                    longPressHandler.postDelayed(
                        longPressedRunnable,
                        ViewConfiguration.getLongPressTimeout().toLong()
                    )

                    handled = true
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (!longPressed && singleMove != 2 && event.pointerCount == 2) {
//                    Log.e("TAG", "onTouchEvent: scale down")
                    doublePointer = 1
                    val centerX = getCenterX(event)
                    val centerY = getCenterY(event)
                    actionX = centerX
                    actionY = centerY
                    spacing = getSpacing(event)
                    degree = getDegree(event)

                    handled = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (longPressed) {      // 长按后，即使手指移动，也不再响应其他事件
                    doublePointer = 0
                    singleMove = 0
                    handled = true
                } else if (singleMove > 0 && event.pointerCount == 1) {
                    doublePointer = 0
//                    Log.e("TAG", "onTouchEvent: move move")
                    val deltaX = x - singleMoveDownX
                    val deltaY = y - singleMoveDownY
                    val absDeltaX = abs(deltaX)
                    val absDeltaY = abs(deltaY)
                    handled = if (singleMove == 2 || absDeltaX > 50 || absDeltaY > 50) {
                        longPressHandler.removeCallbacks(longPressedRunnable)   // 取消长按监听
                        singleMove = 2
                        mListener.onSingleMoving(deltaX, deltaY, x, y)
                    } else false
                } else if (doublePointer == 1 && event.pointerCount == 2) {
                    longPressHandler.removeCallbacks(longPressedRunnable)       // 取消长按监听
                    singleMove = 0
//                    Log.e("TAG", "onTouchEvent: scale move")
                    val centerX = getCenterX(event)
                    val centerY = getCenterY(event)
                    translationX = lastTranslationX + centerX - actionX
                    translationY = lastTranslationY + centerY - actionY
                    actionX = centerX
                    actionY = centerY

                    scale = lastScale * getSpacing(event) / spacing
                    rotation = lastRotation + getDegree(event) - degree
                    // 把rotation限制在[-359, 359]
                    rotation %= 360

                    if (!isZoom &&
                        (translationX != 0f || translationY != 0f || scale != 1f || rotation != 0f)
                    ) {
                        isZoom = true
                        mListener.isZoomChanged(true)
                    }

                    handled = mListener.onZoomUpdate(this)
                } else {
                    doublePointer = 0
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                longPressHandler.removeCallbacks(longPressedRunnable)           // 取消长按监听
                if (longPressed && event.pointerCount == 1) {
                    handled = mListener.onLongPressUp()
                    longPressed = false
                } else if (singleMove > 0/* && event.pointerCount == 1*/) {
                    // 单指滑动了一段距离，第二个手指又落下滑动，然后两个手指抬起
                    // 这时候应该只响应单指滑动，因为它先产生的。
                    // 所以这时event.pointerCount可能不1，所以不能加&& event.pointerCount == 1
                    val deltaX = x - singleMoveDownX
                    val deltaY = y - singleMoveDownY
                    handled = mListener.onSingleMoved(deltaX, deltaY, x, y)
                } else if (doublePointer == 1 && event.pointerCount == 2) {
                    doublePointer = 0
                    lastScale = scale
                    lastTranslationX = translationX
                    lastTranslationY = translationY
                    lastRotation = rotation

                    handled = true
                }
                singleMove = 0
                doublePointer = 0
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

    interface PlayerGestureListener {
        fun onZoomUpdate(detector: PlayerGestureDetector): Boolean = false
        fun onSingleMoving(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean = false
        fun onSingleMoved(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean = false
        fun isZoomChanged(isZoom: Boolean) {}
        fun onLongPressUp(): Boolean = false
        fun onLongPress() {}
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}