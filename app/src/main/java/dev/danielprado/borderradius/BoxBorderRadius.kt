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

private data class RadiusSize(val horizontal: Float, val vertical: Float)

class BoxBorderRadius(context: Context, attrs: AttributeSet?): View(context, attrs) {
    private var horizontalCenter = 0f
    private var verticalCenter = 0f
    private var minCenter = 0f
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
                borderRadiusString = getString(R.styleable.BoxBorderRadius_borderRadius) ?: ""
                buildPath(parseBorderRadius())
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
            invalidate()
        }

    var borderRadius: String
        get() = this.borderRadiusString
        set(value) {
            borderRadiusString = value
            buildPath(parseBorderRadius())
            invalidate()
        }

    private fun parseBorderRadius(): Array<RadiusSize> {
        if (borderRadiusString.isNotEmpty()) {
            val matches = borderRadiusRegex.findAll(borderRadiusString)
            return when (matches.count()) {
                4 -> setRadiusFrom4Values(matches)
                3 -> setRadiusFrom3Values(matches)
                2 -> setRadiusFrom2Values(matches)
                else -> setRadiusFrom1Value(matches)
            }

        } else {
            return arrayOf(
                    RadiusSize(0f, 0f),
                    RadiusSize(0f, 0f),
                    RadiusSize(0f, 0f),
                    RadiusSize(0f, 0f)
            )
        }
    }

    private fun newRadiusSizeFromPercentage(horizontal: Float, vertical: Float): RadiusSize {
        val hSize = if (horizontal <= horizontalCenter) horizontal else horizontalCenter
        val vSize = if (vertical <= verticalCenter) vertical else verticalCenter
        return RadiusSize(hSize, vSize)
    }

    private fun newRadiusSizeFromValue(value: Float): RadiusSize {
        val size = minOf(value, minCenter)
        return RadiusSize(size, size)
    }

    private fun getRadiusValue(match: MatchResult): RadiusSize {
        val unit = match.groupValues[2]
        val value = match.groupValues[1].toFloat()
        return when(unit) {
            "em" -> newRadiusSizeFromValue(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics))
            "%" -> newRadiusSizeFromPercentage(width/2 * value/100, height/2 * value/100) // %
            else -> newRadiusSizeFromValue(value) // px
        }
    }

    private fun setRadiusFrom4Values(matches: Sequence<MatchResult>): Array<RadiusSize> {
        return arrayOf(
                getRadiusValue(matches.elementAt(0)),
                getRadiusValue(matches.elementAt(1)),
                getRadiusValue(matches.elementAt(2)),
                getRadiusValue(matches.elementAt(3))
        )
    }

    private fun setRadiusFrom3Values(matches: Sequence<MatchResult>): Array<RadiusSize> {
        val topRightBottomLeft = getRadiusValue(matches.elementAt(1))
        return arrayOf(
                getRadiusValue(matches.elementAt(0)),
                topRightBottomLeft,
                getRadiusValue(matches.elementAt(2)),
                topRightBottomLeft
        )
    }

    private fun setRadiusFrom2Values(matches: Sequence<MatchResult>): Array<RadiusSize> {
        val topLeftBottomRight = getRadiusValue(matches.elementAt(0))
        val topRightBottomLeft = getRadiusValue(matches.elementAt(1))
        return arrayOf(
                topLeftBottomRight,
                topRightBottomLeft,
                topLeftBottomRight,
                topRightBottomLeft
        )
    }

    private fun setRadiusFrom1Value(matches: Sequence<MatchResult>): Array<RadiusSize> {
        val allRadius = getRadiusValue(matches.elementAt(0));
        return arrayOf(
                allRadius,
                allRadius,
                allRadius,
                allRadius
        )
    }

    private fun buildPath(radius: Array<RadiusSize>) {
        val width = this.width.toFloat() - 1
        val height = this.height.toFloat() - 1
        path = Path().apply {
            moveTo(0 + radius[0].horizontal, 0f)

            // top line
            lineTo(width - radius[1].horizontal, 0f)
            // top right radius
            cubicTo(width - radius[1].horizontal, 0f, width, 0f, width, 0 + radius[1].vertical)

            // right line
            lineTo(width, height - radius[2].vertical);
            // bottom right radius
            cubicTo(width, height - radius[2].vertical, width, height, width - radius[2].horizontal, height)

            // bottom line
            lineTo(0 + radius[3].horizontal, height);
            // bottom left radius
            cubicTo(0f + radius[3].horizontal, height, 0f, height, 0f, height - radius[3].vertical)

            // left line
            lineTo(0f, 0f + radius[0].vertical)
            cubicTo(0f, 0f + radius[0].vertical, 0f, 0f, 0f + radius[0].horizontal, 0f)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (path == null)
            return

        canvas?.apply {
            drawPath(path!!, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        horizontalCenter = w.toFloat()/2
        verticalCenter = h.toFloat()/2
        minCenter = minOf(horizontalCenter, verticalCenter)
        buildPath(parseBorderRadius())
    }
}