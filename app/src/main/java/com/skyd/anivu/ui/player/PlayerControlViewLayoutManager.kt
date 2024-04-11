/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skyd.anivu.ui.player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import androidx.media3.ui.DefaultTimeBar
import com.skyd.anivu.R
import kotlin.math.max

@SuppressLint("UnsafeOptInUsageError", "PrivateResource")
internal class PlayerControlViewLayoutManager(private val playerControlView: PlayerControlView) {
    // ViewGroup that can be automatically hidden
    private val autoHiddenControllerView: ViewGroup =
        playerControlView.findViewById(R.id.exo_auto_hidden_control_view)

    // Relating to Center View
    private val controlsBackground: View? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_controls_background)
    private val centerControls: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_center_controls)

    // Relating to Bottom Bar View
    private val bottomBar: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_bottom_bar)

    // Relating to Top Bar View
    private val topBar: ViewGroup? = playerControlView.findViewById(R.id.exo_top_bar)

    // Relating to Reset Zoom View
    private val resetZoomButton: View? = playerControlView.findViewById(R.id.exo_reset_zoom)

    // Relating to Forward 85s View
    private val forward85sButton: View? = playerControlView.findViewById(R.id.exo_forward_85s)

    // Relating to Minimal Layout
    private val minimalControls: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_minimal_controls)

    // Relating to Bottom Bar Right View
    private val basicControls: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_basic_controls)
    private val extraControls: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_extra_controls)
    private val extraControlsScrollView: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_extra_controls_scroll_view)
    private val overflowHideButton =
        playerControlView.findViewById<View>(androidx.media3.ui.R.id.exo_overflow_hide)

    // Relating to Bottom Bar Left View
    private val timeView: ViewGroup? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_time)
    private val timeBar: View? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_progress)

    private val overflowShowButton: View? =
        playerControlView.findViewById(androidx.media3.ui.R.id.exo_overflow_show)
    private val hideMainBarAnimator: AnimatorSet = AnimatorSet()
    private val hideProgressBarAnimator: AnimatorSet = AnimatorSet()
    private val hideAllBarsAnimator: AnimatorSet = AnimatorSet()
    private val showMainBarAnimator: AnimatorSet = AnimatorSet()
    private val showAllBarsAnimator: AnimatorSet = AnimatorSet()
    private val overflowShowAnimator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    private val overflowHideAnimator: ValueAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
    private val showAllBarsRunnable: Runnable = Runnable { showAllBars() }
    private val hideAllBarsRunnable: Runnable = Runnable { hideAllBars() }
    private val hideProgressBarRunnable: Runnable = Runnable { hideProgressBar() }
    private val hideMainBarRunnable: Runnable = Runnable { hideMainBar() }
    private val hideControllerRunnable: Runnable = Runnable { hideController() }
    private val onLayoutChangeListener: OnLayoutChangeListener =
        OnLayoutChangeListener { v: View, left: Int, _: Int, right: Int, _: Int,
                                 oldLeft: Int, _: Int, oldRight: Int, _: Int ->
            onLayoutChange(v, left, right, oldLeft, oldRight)
        }
    private val shownButtons: MutableList<View> = mutableListOf()
    private var uxState: Int = UX_STATE_ALL_VISIBLE
    private var isMinimalMode = false
    private var needToShowBars = false
    var isAnimationEnabled: Boolean = true

    init {
        if (overflowShowButton != null && overflowHideButton != null) {
            overflowShowButton.setOnClickListener(::onOverflowButtonClick)
            overflowHideButton.setOnClickListener(::onOverflowButtonClick)
        }

        val fadeOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
        fadeOutAnimator.interpolator = LinearInterpolator()
        fadeOutAnimator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.getAnimatedValue() as Float
            controlsBackground?.setAlpha(animatedValue)
            centerControls?.setAlpha(animatedValue)
            minimalControls?.setAlpha(animatedValue)
        }
        fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                if (timeBar is DefaultTimeBar && !isMinimalMode) {
                    timeBar.hideScrubber(DURATION_FOR_HIDING_ANIMATION_MS)
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                controlsBackground?.visibility = View.INVISIBLE
                centerControls?.visibility = View.INVISIBLE
                minimalControls?.visibility = View.INVISIBLE
            }
        })

        val fadeInAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        fadeInAnimator.interpolator = LinearInterpolator()
        fadeInAnimator.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue() as Float
            controlsBackground?.setAlpha(animatedValue)
            centerControls?.setAlpha(animatedValue)
            minimalControls?.setAlpha(animatedValue)
        }
        fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                controlsBackground?.visibility = View.VISIBLE
                centerControls?.visibility = View.VISIBLE
                minimalControls?.visibility = if (isMinimalMode) View.VISIBLE else View.INVISIBLE
                if (timeBar is DefaultTimeBar && !isMinimalMode) {
                    timeBar.showScrubber(DURATION_FOR_SHOWING_ANIMATION_MS)
                }
            }
        })

        hideMainBarAnimator.setDuration(DURATION_FOR_HIDING_ANIMATION_MS)
        hideMainBarAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) = setUxState(UX_STATE_ANIMATING_HIDE)

            override fun onAnimationEnd(animation: Animator) {
                setUxState(UX_STATE_ONLY_PROGRESS_VISIBLE)
                if (needToShowBars) {
                    playerControlView.post(showAllBarsRunnable)
                    needToShowBars = false
                }
            }
        })
        hideMainBarAnimator
            .play(fadeOutAnimator)
            .with(ofAlpha(1f, 0f, timeBar))
            .with(ofAlpha(1f, 0f, bottomBar))
            .with(ofAlpha(1f, 0f, topBar))
            .with(ofAlpha(1f, 0f, resetZoomButton))
            .with(ofAlpha(1f, 0f, forward85sButton))

        hideProgressBarAnimator.setDuration(DURATION_FOR_HIDING_ANIMATION_MS)
        hideProgressBarAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) = setUxState(UX_STATE_ANIMATING_HIDE)

            override fun onAnimationEnd(animation: Animator) {
                setUxState(UX_STATE_NONE_VISIBLE)
                if (needToShowBars) {
                    playerControlView.post(showAllBarsRunnable)
                    needToShowBars = false
                }
            }
        })
        hideProgressBarAnimator
            .play(ofAlpha(1f, 0f, timeBar))
            .with(ofAlpha(1f, 0f, bottomBar))
            .with(ofAlpha(1f, 0f, topBar))
            .with(ofAlpha(1f, 0f, resetZoomButton))
            .with(ofAlpha(1f, 0f, forward85sButton))

        hideAllBarsAnimator.setDuration(DURATION_FOR_HIDING_ANIMATION_MS)
        hideAllBarsAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) = setUxState(UX_STATE_ANIMATING_HIDE)

            override fun onAnimationEnd(animation: Animator) {
                setUxState(UX_STATE_NONE_VISIBLE)
                if (needToShowBars) {
                    playerControlView.post(showAllBarsRunnable)
                    needToShowBars = false
                }
            }
        })
        hideAllBarsAnimator
            .play(fadeOutAnimator)
            .with(ofAlpha(1f, 0f, timeBar))
            .with(ofAlpha(1f, 0f, bottomBar))
            .with(ofAlpha(1f, 0f, topBar))
            .with(ofAlpha(1f, 0f, resetZoomButton))
            .with(ofAlpha(1f, 0f, forward85sButton))

        showMainBarAnimator.setDuration(DURATION_FOR_SHOWING_ANIMATION_MS)
        showMainBarAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) = setUxState(UX_STATE_ANIMATING_SHOW)
            override fun onAnimationEnd(animation: Animator) = setUxState(UX_STATE_ALL_VISIBLE)
        })
        showMainBarAnimator
            .play(fadeInAnimator)
            .with(ofAlpha(0f, 1f, timeBar))
            .with(ofAlpha(0f, 1f, bottomBar))
            .with(ofAlpha(0f, 1f, topBar))
            .with(ofAlpha(0f, 1f, resetZoomButton))
            .with(ofAlpha(0f, 1f, forward85sButton))

        showAllBarsAnimator.setDuration(DURATION_FOR_SHOWING_ANIMATION_MS)
        showAllBarsAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) = setUxState(UX_STATE_ANIMATING_SHOW)
            override fun onAnimationEnd(animation: Animator) = setUxState(UX_STATE_ALL_VISIBLE)
        })
        showAllBarsAnimator
            .play(fadeInAnimator)
            .with(ofAlpha(0f, 1f, timeBar))
            .with(ofAlpha(0f, 1f, bottomBar))
            .with(ofAlpha(0f, 1f, topBar))
            .with(ofAlpha(0f, 1f, resetZoomButton))
            .with(ofAlpha(0f, 1f, forward85sButton))

        overflowShowAnimator.setDuration(DURATION_FOR_SHOWING_ANIMATION_MS)
        overflowShowAnimator.addUpdateListener { animation ->
            animateOverflow(animation.getAnimatedValue() as Float)
        }
        overflowShowAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                extraControlsScrollView?.apply {
                    visibility = View.VISIBLE
                    translationX = width.toFloat()
                    scrollTo(width, 0)
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                basicControls?.visibility = View.INVISIBLE
            }
        })

        overflowHideAnimator.setDuration(DURATION_FOR_SHOWING_ANIMATION_MS)
        overflowHideAnimator.addUpdateListener { animation ->
            animateOverflow(animation.getAnimatedValue() as Float)
        }
        overflowHideAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                basicControls?.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                extraControlsScrollView?.visibility = View.INVISIBLE
            }
        })
    }

    fun show() {
        if (!playerControlView.isAutoHiddenControllerVisible) {
            autoHiddenControllerView.visibility = View.VISIBLE
            playerControlView.updateAll()
            playerControlView.requestPlayPauseFocus()
        }
        showAllBars()
    }

    fun hide() {
        if (uxState == UX_STATE_ANIMATING_HIDE || uxState == UX_STATE_NONE_VISIBLE) {
            return
        }
        removeHideCallbacks()
        if (!isAnimationEnabled) {
            hideController()
        } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
            hideProgressBar()
        } else {
            hideAllBars()
        }
    }

    fun hideImmediately() {
        if (uxState == UX_STATE_ANIMATING_HIDE || uxState == UX_STATE_NONE_VISIBLE) {
            return
        }
        removeHideCallbacks()
        hideController()
    }

    fun resetHideCallbacks() {
        if (uxState == UX_STATE_ANIMATING_HIDE) {
            return
        }
        removeHideCallbacks()
        val showTimeoutMs = playerControlView.showTimeoutMs
        if (showTimeoutMs > 0) {
            if (!isAnimationEnabled) {
                postDelayedRunnable(hideControllerRunnable, showTimeoutMs.toLong())
            } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
                postDelayedRunnable(hideProgressBarRunnable, ANIMATION_INTERVAL_MS)
            } else {
                postDelayedRunnable(hideMainBarRunnable, showTimeoutMs.toLong())
            }
        }
    }

    fun removeHideCallbacks() {
        playerControlView.apply {
            removeCallbacks(hideControllerRunnable)
            removeCallbacks(hideAllBarsRunnable)
            removeCallbacks(hideMainBarRunnable)
            removeCallbacks(hideProgressBarRunnable)
        }
    }

    fun onAttachedToWindow() {
        playerControlView.addOnLayoutChangeListener(onLayoutChangeListener)
    }

    fun onDetachedFromWindow() {
        playerControlView.removeOnLayoutChangeListener(onLayoutChangeListener)
    }

    val isFullyVisible: Boolean
        get() = uxState == UX_STATE_ALL_VISIBLE && playerControlView.isAutoHiddenControllerVisible

    fun setShowButton(button: View?, showButton: Boolean) {
        button ?: return
        if (!showButton) {
            button.visibility = View.GONE
            shownButtons.remove(button)
            return
        }
        button.visibility = if (isMinimalMode && shouldHideInMinimalMode(button)) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        shownButtons.add(button)
    }

    fun getShowButton(button: View?): Boolean {
        return button != null && shownButtons.contains(button)
    }

    private fun setUxState(uxState: Int) {
        val prevUxState = this.uxState
        this.uxState = uxState
        if (uxState == UX_STATE_NONE_VISIBLE) {
            autoHiddenControllerView.visibility = View.GONE
        } else if (prevUxState == UX_STATE_NONE_VISIBLE) {
            autoHiddenControllerView.visibility = View.VISIBLE
        }
        // TODO(insun): Notify specific uxState. Currently reuses legacy visibility listener for API
        //  compatibility.
        if (prevUxState != uxState) {
            playerControlView.notifyOnVisibilityChange()
        }
    }

    fun onLayout(left: Int, top: Int, right: Int, bottom: Int) {
        controlsBackground?.layout(0, 0, right - left, bottom - top)
    }

    private fun onLayoutChange(v: View, left: Int, right: Int, oldLeft: Int, oldRight: Int) {
        val useMinimalMode = useMinimalMode()
        if (isMinimalMode != useMinimalMode) {
            isMinimalMode = useMinimalMode
            v.post(::updateLayoutForSizeChange)
        }
        val widthChanged = right - left != oldRight - oldLeft
        if (!isMinimalMode && widthChanged) {
            v.post(::onLayoutWidthChanged)
        }
    }

    private fun onOverflowButtonClick(v: View) {
        resetHideCallbacks()
        if (v.id == androidx.media3.ui.R.id.exo_overflow_show) {
            overflowShowAnimator.start()
        } else if (v.id == androidx.media3.ui.R.id.exo_overflow_hide) {
            overflowHideAnimator.start()
        }
    }

    private fun showAllBars() {
        if (!isAnimationEnabled) {
            setUxState(UX_STATE_ALL_VISIBLE)
            resetHideCallbacks()
            return
        }
        when (uxState) {
            UX_STATE_NONE_VISIBLE -> showAllBarsAnimator.start()
            UX_STATE_ONLY_PROGRESS_VISIBLE -> showMainBarAnimator.start()
            UX_STATE_ANIMATING_HIDE -> needToShowBars = true
            UX_STATE_ANIMATING_SHOW -> return
            else -> Unit
        }
        resetHideCallbacks()
    }

    private fun hideAllBars() {
        hideAllBarsAnimator.start()
    }

    private fun hideProgressBar() {
        hideProgressBarAnimator.start()
    }

    private fun hideMainBar() {
        hideMainBarAnimator.start()
        postDelayedRunnable(hideProgressBarRunnable, ANIMATION_INTERVAL_MS)
    }

    private fun hideController() {
        setUxState(UX_STATE_NONE_VISIBLE)
    }

    private fun postDelayedRunnable(runnable: Runnable, interval: Long) {
        if (interval >= 0) {
            playerControlView.postDelayed(runnable, interval)
        }
    }

    private fun animateOverflow(animatedValue: Float) {
        extraControlsScrollView?.apply {
            translationX = width * (1 - animatedValue)
        }
        timeView?.setAlpha(1 - animatedValue)
        basicControls?.setAlpha(1 - animatedValue)
    }

    private fun useMinimalMode(): Boolean {
        val width = playerControlView.width -
                playerControlView.getPaddingLeft() -
                playerControlView.getPaddingRight()
        val height = playerControlView.height -
                playerControlView.paddingBottom -
                playerControlView.paddingTop
        val centerControlWidth = getWidthWithMargins(centerControls) -
                if (centerControls != null) {
                    centerControls.getPaddingLeft() + centerControls.getPaddingRight()
                } else 0
        val centerControlHeight = getHeightWithMargins(centerControls) -
                if (centerControls != null) {
                    centerControls.paddingTop + centerControls.paddingBottom
                } else 0
        val defaultModeMinimumWidth = max(
            centerControlWidth,
            getWidthWithMargins(timeView) + getWidthWithMargins(overflowShowButton)
        )
        val defaultModeMinimumHeight = (centerControlHeight + getHeightWithMargins(bottomBar)
                + getHeightWithMargins(topBar)) /* + (2 * getHeightWithMargins(bottomBar))*/
        return width <= defaultModeMinimumWidth || height <= defaultModeMinimumHeight
    }

    private fun updateLayoutForSizeChange() {
        minimalControls?.visibility = if (isMinimalMode) View.VISIBLE else View.INVISIBLE
        if (timeBar != null) {
            val timeBarMarginBottom = playerControlView.resources
                .getDimensionPixelSize(androidx.media3.ui.R.dimen.exo_styled_progress_margin_bottom)
            val timeBarParams = timeBar.layoutParams as? MarginLayoutParams
            if (timeBarParams != null) {
                timeBarParams.bottomMargin = if (isMinimalMode) 0 else timeBarMarginBottom
                timeBar.setLayoutParams(timeBarParams)
            }
            if (timeBar is DefaultTimeBar) {
                if (isMinimalMode) {
                    timeBar.hideScrubber(/* disableScrubberPadding= */ true)
                } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
                    timeBar.hideScrubber(/* disableScrubberPadding= */ false)
                } else if (uxState != UX_STATE_ANIMATING_HIDE) {
                    timeBar.showScrubber()
                }
            }
        }
        for (v in shownButtons) {
            v.visibility =
                if (isMinimalMode && shouldHideInMinimalMode(v)) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun shouldHideInMinimalMode(button: View): Boolean {
        val id = button.id
        return id == androidx.media3.ui.R.id.exo_bottom_bar ||
                id == androidx.media3.ui.R.id.exo_prev ||
                id == androidx.media3.ui.R.id.exo_next ||
                id == androidx.media3.ui.R.id.exo_rew ||
                id == androidx.media3.ui.R.id.exo_rew_with_amount ||
                id == androidx.media3.ui.R.id.exo_ffwd ||
                id == androidx.media3.ui.R.id.exo_ffwd_with_amount
    }

    private fun onLayoutWidthChanged() {
        if (basicControls == null || extraControls == null) return
        val width = playerControlView.width -
                playerControlView.getPaddingLeft() -
                playerControlView.getPaddingRight()

        // Reset back to all controls being basic controls and the overflow not being needed. The last
        // child of extraControls is the overflow hide button, which shouldn't be moved back.
        while (extraControls.childCount > 1) {
            val controlViewIndex = extraControls.childCount - 2
            val controlView = extraControls.getChildAt(controlViewIndex)
            extraControls.removeViewAt(controlViewIndex)
            basicControls.addView(controlView, /* index= */ 0)
        }
        overflowShowButton?.visibility = View.GONE

        // Calculate how much of the available width is occupied. The last child of basicControls is the
        // overflow show button, which we're currently assuming will not be visible.
        var occupiedWidth = getWidthWithMargins(timeView)
        val endIndex = basicControls.childCount - 1
        for (i in 0 until endIndex) {
            val controlView = basicControls.getChildAt(i)
            occupiedWidth += getWidthWithMargins(controlView)
        }
        if (occupiedWidth > width) {
            // We need to move some controls to extraControls.
            overflowShowButton?.visibility = View.VISIBLE
            occupiedWidth += getWidthWithMargins(overflowShowButton)
            val controlsToMove = ArrayList<View>()
            // The last child of basicControls is the overflow show button, which shouldn't be moved.
            for (i in 0 until endIndex) {
                val control = basicControls.getChildAt(i)
                occupiedWidth -= getWidthWithMargins(control)
                controlsToMove.add(control)
                if (occupiedWidth <= width) {
                    break
                }
            }
            if (controlsToMove.isNotEmpty()) {
                basicControls.removeViews(/* start= */ 0, controlsToMove.size)
                for (i in controlsToMove.indices) {
                    // The last child of extraControls is the overflow hide button. Add controls before it.
                    val index = extraControls.childCount - 1
                    extraControls.addView(controlsToMove[i], index)
                }
            }
        } else {
            // If extraControls are visible, hide them since they're now empty.
            if (extraControlsScrollView?.visibility == View.VISIBLE &&
                !overflowHideAnimator.isStarted
            ) {
                overflowShowAnimator.cancel()
                overflowHideAnimator.start()
            }
        }
    }

    companion object {
        private const val ANIMATION_INTERVAL_MS: Long = 2000
        private const val DURATION_FOR_HIDING_ANIMATION_MS: Long = 250
        private const val DURATION_FOR_SHOWING_ANIMATION_MS: Long = 250

        // Int for defining the UX state where all the views (ProgressBar, BottomBar) are all visible.
        private const val UX_STATE_ALL_VISIBLE = 0

        // Int for defining the UX state where only the ProgressBar view is visible.
        private const val UX_STATE_ONLY_PROGRESS_VISIBLE = 1

        // Int for defining the UX state where none of the views are visible.
        private const val UX_STATE_NONE_VISIBLE = 2

        // Int for defining the UX state where the views are being animated to be hidden.
        private const val UX_STATE_ANIMATING_HIDE = 3

        // Int for defining the UX state where the views are being animated to be shown.
        private const val UX_STATE_ANIMATING_SHOW = 4

        private fun ofAlpha(startValue: Float, endValue: Float, target: View?): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "alpha", startValue, endValue)
        }

        private fun getWidthWithMargins(v: View?): Int {
            v ?: return 0
            var width = v.width
            val layoutParams = v.layoutParams
            if (layoutParams is MarginLayoutParams) {
                width += layoutParams.leftMargin + layoutParams.rightMargin
            }
            return width
        }

        private fun getHeightWithMargins(v: View?): Int {
            v ?: return 0
            var height = v.height
            val layoutParams = v.layoutParams
            if (layoutParams is MarginLayoutParams) {
                height += layoutParams.topMargin + layoutParams.bottomMargin
            }
            return height
        }
    }
}
