package dev.danielprado.borderradius

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class BoxBorderRadius(context: Context, attrs: AttributeSet?): View(context, attrs) {
    private var topLeftRadius = 0f
    private var topRightRadius = 0f
    private var bottomRightRadius = 0f
    private var bottomLeftRadius = 0f
    private var path: Path? = null
    private var borderRadiusString = ""
    private var borderRadiusRegex = Regex("(?:(\\d+)(px|em|%)){1,4}", RegexOption.IGNORE_CASE)
    private val displayMetrics = Resources.getSystem().displayMetrics
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BoxBorderRadius, 0, 0).apply {
            try {
                paint.color = getColor(R.styleable.BoxBorderRadius_bkgColor, Color.BLACK)
                borderRadius = getString(R.styleable.BoxBorderRadius_borderRadius) ?: ""
            }
            finally {
                recycle()
            }

        }
    }

    var bkgColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
        }

    var borderRadius: String
        get() = this.borderRadiusString
        set(value) {
            if (value.isNotEmpty()) {
                val matches = borderRadiusRegex.findAll(value)
                when(matches.count()) {
                    4 -> setRadiusFrom4Values(matches)
                    3 -> setRadiusFrom3Values(matches)
                    2 -> setRadiusFrom2Values(matches)
                    1 -> setRadiusFrom1Value(matches)
                }

            } else {
                topLeftRadius = 0f
                topRightRadius = 0f
                bottomLeftRadius = 0f
                bottomRightRadius = 0f
            }
            borderRadiusString = value
        }

    private fun getRadiusValue(match: MatchResult): Float {
        val unit = match.groupValues[2]
        val value = match.groupValues[1].toFloat()
        return when(unit) {
            "em" -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics)
            "%" -> width * value / 100 // %
            else -> value // px
        }
    }

    private fun setRadiusFrom4Values(matches: Sequence<MatchResult>) {
        topLeftRadius = getRadiusValue(matches.elementAt(0))
        topRightRadius = getRadiusValue(matches.elementAt(1))
        bottomRightRadius = getRadiusValue(matches.elementAt(2))
        bottomLeftRadius = getRadiusValue(matches.elementAt(3))
    }

    private fun setRadiusFrom3Values(matches: Sequence<MatchResult>) {
        topLeftRadius = getRadiusValue(matches.elementAt(0))
        topRightRadius = getRadiusValue(matches.elementAt(1))
        bottomLeftRadius = topRightRadius
        bottomRightRadius = getRadiusValue(matches.elementAt(2))
    }

    private fun setRadiusFrom2Values(matches: Sequence<MatchResult>) {
        topLeftRadius = getRadiusValue(matches.elementAt(0))
        bottomRightRadius = topLeftRadius
        topRightRadius = getRadiusValue(matches.elementAt(1))
        bottomLeftRadius = topRightRadius
    }

    private fun setRadiusFrom1Value(matches: Sequence<MatchResult>) {
        topLeftRadius = getRadiusValue(matches.elementAt(0))
        topRightRadius = topLeftRadius
        bottomRightRadius = topLeftRadius
        bottomLeftRadius = topLeftRadius
    }

    private fun limitBorderRadius(controlValue: Float) {
        if (topRightRadius > controlValue)
            topRightRadius = controlValue

        if (topLeftRadius > controlValue)
            topLeftRadius = controlValue

        if (bottomRightRadius > controlValue)
            bottomRightRadius = controlValue

        if (bottomLeftRadius > controlValue)
            bottomRightRadius = controlValue
    }

    override fun onDraw(canvas: Canvas?) {
        if (path == null)
            return

        canvas?.apply {
            drawPath(path!!, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val width = (w - 1).toFloat()
        val height = (h - 1).toFloat()
        limitBorderRadius(width/2)

        path = Path().apply {
            moveTo(0 + topLeftRadius, 0f)

            // top line
            lineTo(width - topRightRadius, 0f)
            // top right radius
            cubicTo(width - topRightRadius, 0f, width, 0f, width, 0 + topRightRadius)

            // right line
            lineTo(width, height - bottomRightRadius);
            // bottom right radius
            cubicTo(width, height - bottomRightRadius, width, height, width - bottomRightRadius, height)

            // bottom line
            lineTo(0 + bottomLeftRadius, height);
            // bottom left radius
            cubicTo(0f + bottomLeftRadius, height, 0f, height, 0f, height - bottomLeftRadius)

            // left line
            lineTo(0f, 0f + topLeftRadius)
            cubicTo(0f, 0f + topLeftRadius, 0f, 0f, 0f + topLeftRadius, 0f)
        }
    }
}