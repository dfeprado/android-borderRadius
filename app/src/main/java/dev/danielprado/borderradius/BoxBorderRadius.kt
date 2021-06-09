package dev.danielprado.borderradius

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Dimension

class BoxBorderRadius(context: Context, attrs: AttributeSet?): View(context, attrs) {
    private var topLeftRadius = 0f
    private var topRightRadius = 0f
    private var bottomRightRadius = 0f
    private var bottomLeftRadius = 0f
    private var path: Path? = null
    private var borderRadiusString = "0px"
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    var bkgColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
        }

//    var borderRadius: String
//        get() = this.borderRadiusString
//        set(value) {
//            value.matches(Regex(""))
//        }

    override fun onDraw(canvas: Canvas?) {
        if (path == null)
            return

        canvas?.apply {
            drawPath(path!!, paint)
        }
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