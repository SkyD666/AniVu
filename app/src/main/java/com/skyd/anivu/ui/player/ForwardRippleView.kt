package com.skyd.anivu.ui.player

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.google.android.material.textview.MaterialTextView
import com.skyd.anivu.R
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.isRunning
import com.skyd.anivu.ext.toDegrees
import kotlin.math.abs
import kotlin.math.atan2


class ForwardRippleView : LinearLayout {
    enum class RippleCenter {
        StartCenter, EndCenter
    }

    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private lateinit var space: Space

    private var rippleCenter: RippleCenter = RippleCenter.StartCenter
    private val ripplePaint: Paint = Paint().apply {
        setColor(Color.parseColor("#77000000"))
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private lateinit var originRippleRect: RectF
    private lateinit var rippleRect: RectF
    private var startAngle: Float = 0f
    private var sweepAngle: Float = 360f
    private var maxRippleRadius = 0f
    private var rippleRadiusExpandSpeed = 35f.dp
    private var hideAnimation = AlphaAnimation(1f, 0f).apply { duration = 400L }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        post { super.setVisibility(GONE) }
        orientation = VERTICAL
        gravity = CENTER
        val a = context!!.obtainStyledAttributes(
            attrs, R.styleable.ForwardRippleView, defStyleAttr, defStyleRes
        )
        initRipple(a)
        initTextView(a)
        initImageView(a)
        initSpace()
        addView(imageView)
        addView(space)
        addView(textView)

        setWillNotDraw(false)

        a.recycle()
    }

    private fun initRipple(a: TypedArray) {
        if (a.hasValue(R.styleable.ForwardRippleView_center)) {
            when (a.getInt(R.styleable.ForwardRippleView_center, 0)) {
                0 -> rippleCenter = RippleCenter.StartCenter
                1 -> rippleCenter = RippleCenter.EndCenter
            }
        }
        if (a.hasValue(R.styleable.ForwardRippleView_expandSpeed)) {
            rippleRadiusExpandSpeed = a.getDimension(
                R.styleable.ForwardRippleView_expandSpeed, rippleRadiusExpandSpeed
            )
        }
        if (a.hasValue(R.styleable.ForwardRippleView_rippleColor)) {
            ripplePaint.setColor(
                a.getColor(R.styleable.ForwardRippleView_rippleColor, ripplePaint.color)
            )
        }
    }

    private fun initTextView(a: TypedArray) {
        textView = MaterialTextView(
            context!!,
            null,
            0,
        ).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_LabelLarge
            )
            if (a.hasValue(R.styleable.ForwardRippleView_text)) {
                text = a.getText(R.styleable.ForwardRippleView_text)
            }
            if (a.hasValue(R.styleable.ForwardRippleView_textColor)) {
                setTextColor(a.getColorStateList(R.styleable.ForwardRippleView_textColor))
            } else {
                setTextColor(Color.WHITE)
            }
        }
    }

    private fun initImageView(a: TypedArray) {
        imageView = ImageView(context).apply {
            layoutParams = LayoutParams(50.dp, 50.dp)
            if (a.hasValue(R.styleable.ForwardRippleView_drawable)) {
                setImageDrawable(a.getDrawable(R.styleable.ForwardRippleView_drawable))
            }
            imageTintList = if (a.hasValue(R.styleable.ForwardRippleView_drawableTint)) {
                a.getColorStateList(R.styleable.ForwardRippleView_drawableTint)
            } else {
                ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    private fun initSpace() {
        space = Space(context).apply {
            layoutParams = LayoutParams(0, 10.dp)
        }
    }

    override fun setVisibility(visibility: Int) {
        when (visibility) {
            VISIBLE -> {
                hideAnimation.cancel()
                super.setVisibility(VISIBLE)
                rippleRect = RectF(originRippleRect)
                invalidate()
            }

            GONE -> {
                if (!hideAnimation.isRunning && getVisibility() == VISIBLE) {
                    startAnimation(hideAnimation)
                }
                super.setVisibility(GONE)
            }

            INVISIBLE -> {
                if (!hideAnimation.isRunning && getVisibility() == VISIBLE) {
                    startAnimation(hideAnimation)
                }
                super.setVisibility(INVISIBLE)
            }
        }
    }

    @JvmOverloads
    fun visible(time: Long = 700L) {
        visibility = View.VISIBLE
        postDelayed({ visibility = View.GONE }, time)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        when (rippleCenter) {
            RippleCenter.StartCenter -> {
                // origin distance from the centerX to view border is w.toFloat()
                originRippleRect = RectF().also { rect ->
                    rect.left = -w.toFloat() * 2
                    rect.right = 0f
                    // use rect.width() because we want the rect is a square
                    rect.top = (h - rect.width()) / 2f
                    rect.bottom = (h + rect.width()) / 2f
                }
                rippleRect = RectF(originRippleRect)
                maxRippleRadius = w - originRippleRect.centerX()

                val halfDeltaAngle = atan2(h / 2f, -rippleRect.centerX()).toDegrees()
                startAngle = -halfDeltaAngle
                sweepAngle = abs(halfDeltaAngle * 2)
            }

            RippleCenter.EndCenter -> {
                // origin distance from the centerX to view border is w.toFloat()
                originRippleRect = RectF().also { rect ->
                    rect.left = w.toFloat()
                    rect.right = 3 * w.toFloat()
                    // use rect.width() because we want the rect is a square
                    rect.top = (h - rect.width()) / 2f
                    rect.bottom = (h + rect.width()) / 2f
                }
                rippleRect = RectF(originRippleRect)
                maxRippleRadius = originRippleRect.centerX()

                val halfDeltaAngle = atan2(h / 2f, rippleRect.centerX() - w).toDegrees()
                startAngle = 180 - halfDeltaAngle
                sweepAngle = abs(halfDeltaAngle * 2)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawArc(rippleRect, startAngle, sweepAngle, true, ripplePaint)
        if (rippleRect.width() / 2 < maxRippleRadius) {
            var delta = rippleRadiusExpandSpeed
            val newWidth = rippleRect.right - rippleRect.left + 2 * delta
            if (newWidth / 2f > maxRippleRadius) {
                delta -= newWidth / 2f - maxRippleRadius
            }
            rippleRect.set(
                /* left = */ rippleRect.left - delta,
                /* top = */ rippleRect.top - delta,
                /* right = */ rippleRect.right + delta,
                /* bottom = */ rippleRect.bottom + delta
            )
            invalidate()
        }
    }

    var text: CharSequence
        get() = textView.text
        set(value) {
            textView.text = value
        }

    var drawable: Drawable?
        get() = imageView.drawable
        set(value) {
            imageView.setImageDrawable(value)
        }
}