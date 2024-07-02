package com.skyd.anivu.ui.component

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
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.skyd.anivu.R
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.visible


class ColorPaletteView : MaterialCardView {
    private val iconImage = ImageView(context)
    var icon: Drawable?
        get() = iconImage.drawable
        set(value) {
            iconImage.setImageDrawable(value)
        }

    var iconBackgroundColor: Int = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorPrimary
    )
        set(value) {
            field = value
            iconImage.backgroundTintList = ColorStateList.valueOf(value)
        }

    var color1: Int = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorPrimaryFixedDim
    )
        set(value) {
            field = value
            invalidate()
        }

    var color2: Int = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorSecondaryFixedDim
    )
        set(value) {
            field = value
            invalidate()
        }

    var color3: Int = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorTertiaryFixedDim
    )
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        with(iconImage) {
            setBackgroundResource(R.drawable.shape_fill_circle)
            setPadding(7.dp)
            backgroundTintList = ColorStateList.valueOf(iconBackgroundColor)
        }
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ColorPaletteView, defStyleAttr, 0
        )
        initBackground(a)
        val padding = a.getDimensionPixelSize(R.styleable.ColorPaletteView_android_padding, 12.dp)
        setContentPadding(padding, padding, padding, padding)
        initIconImage(a)
        addView(iconImage)

        a.recycle()
    }

    private fun initBackground(a: TypedArray) {
        radius =
            a.getDimensionPixelSize(R.styleable.ColorPaletteView_cardCornerRadius, 16.dp).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(
            MaterialColors.getColorStateList(
                context,
                com.google.android.material.R.attr.colorSurfaceContainerHigh,
                ColorStateList.valueOf((Color.BLACK and 0x00FFFFFF) or 0x66000000),
            )
        )
    }

    private fun initIconImage(a: TypedArray) {
        iconImage.layoutParams = LayoutParams(30.dp, 30.dp).apply {
            gravity = CENTER
        }
        iconImage.imageTintList = ColorStateList.valueOf(Color.WHITE)
        if (a.hasValue(R.styleable.ColorPaletteView_icon)) {
            iconImage.setImageDrawable(a.getDrawable(R.styleable.ColorPaletteView_icon))
        } else {
            iconImage.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_done_24,
                    context.theme
                )
            )
        }
    }

    private lateinit var ovalRect: RectF
    private val arcPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        ovalRect = RectF(
            paddingStart.toFloat(),
            paddingTop.toFloat(),
            (w - paddingEnd).toFloat(),
            (h - paddingBottom).toFloat(),
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        arcPaint.setColor(color1)
        canvas.drawArc(ovalRect, 180f, 180f, true, arcPaint)
        arcPaint.setColor(color2)
        canvas.drawArc(ovalRect, 90f, 90f, true, arcPaint)
        arcPaint.setColor(color3)
        canvas.drawArc(ovalRect, 0f, 90f, true, arcPaint)
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        if (checked) iconImage.visible(true) else iconImage.gone(true)
    }
}