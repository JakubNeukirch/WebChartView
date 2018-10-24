package pl.jakubneukirch.webchart

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

private const val MIN_ARM_COUNT = 3
private const val MAX_ARM_COUNT = 10

private const val SCALE_LINES_COUNT = 5
private const val LABEL_BACKGROUND_OFFSET = 5

class WebChartView : View {

    var points: List<Point> = listOf()
        set(value) {
            field = value
            armCount = points.size
            redraw()
        }

    var labelTextSize: Float = context.resources.getDimension(R.dimen.label_size)
        set(value) {
            field = value
            labelPaint.textSize = labelTextSize
            redraw()
        }

    var webColor = Color.BLUE
        set(value) {
            field = value
            pointsPaintFill.color = value
            pointsPaintFill.alpha = fillAlpha
            pointsPaintStroke.color = value
            pointsShadowPaint.color = value
            redraw()
        }

    private var pointsStrokeWidth = 4f
        set(value) {
            field = value
            pointsPaintStroke.strokeWidth = value
            redraw()
        }

    private var armCount = 5
        set(value) {
            field = when {
                value < MIN_ARM_COUNT -> MIN_ARM_COUNT
                value > MAX_ARM_COUNT -> MAX_ARM_COUNT
                else -> value
            }
        }

    private var webPath = Path()
    private var pointsPath = Path()
    private var scalePath = Path()

    private var fillAlpha = 128
    private var armLength = 20
    private var angle = 360f / armCount
    private var scaleSpace = 10f
    private var labelBackgroundRadius = 5f
    private var labelElevation = 9f
    private var innerPadding = 10

    private var bitmapMatrix = Matrix()
    private var canvasBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var canvas = Canvas(canvasBitmap)

    private var bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var webPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private var pointsPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = webColor
        strokeWidth = pointsStrokeWidth
    }
    private var pointsPaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = webColor
        alpha = fillAlpha
    }

    private var scalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = ContextCompat.getColor(context, R.color.scale_color)
    }

    private var labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = labelTextSize
    }

    private var labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private var shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = pointsStrokeWidth
        maskFilter = BlurMaskFilter(labelElevation, BlurMaskFilter.Blur.NORMAL)
    }

    private var pointsShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = webColor
        style = Paint.Style.STROKE
        strokeWidth = pointsStrokeWidth
        maskFilter = BlurMaskFilter(labelElevation, BlurMaskFilter.Blur.NORMAL)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.WebChartView)
        labelTextSize = array.getDimension(R.styleable.WebChartView_labelTextSize, context.resources.getDimension(R.dimen.label_size))
        webColor = array.getColor(R.styleable.WebChartView_webColor, Color.BLUE)
        array.recycle()
        redraw()
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
        setLayerType(LAYER_TYPE_SOFTWARE, pointsShadowPaint)
    }

    private fun redraw() {
        invalidateData()
        with(canvas) {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawPath(scalePath, scalePaint)
            drawPath(webPath, webPaint)
            drawPath(pointsPath, pointsShadowPaint)
            drawPath(pointsPath, pointsPaintFill)
            drawPath(pointsPath, pointsPaintStroke)
        }
        drawText()
        invalidate()
    }

    private fun invalidateData() {
        invalidateValues()
        invalidateWebPath()
        invalidateScalePath()
        invalidatePointsPath()
    }

    private fun invalidateValues() {
        angle = 360f / armCount
        armLength = canvasBitmap.width / 2
        scaleSpace = armLength / SCALE_LINES_COUNT.toFloat()
    }

    private fun invalidateWebPath() {
        webPath = Path()
        for (i in 0 until armCount) {
            webPath.moveToCenter()
            webPath.drawArm(i)
        }
    }

    private fun drawText() {
        var coord: Pair<Float, Float>
        val bounds = Rect()
        val last = Math.min(points.size, MAX_ARM_COUNT)
        var value: String
        var x: Float
        var y: Float
        var rect: RectF
        for (i in 0 until last) {
            value = points[i].caption
            if (value.isNotEmpty()) {
                coord = getPointAtArm(i, armLength.toFloat())
                labelPaint.getTextBounds(value, 0, value.length, bounds)

                x = getTextX(coord, bounds)
                y = getTextY(coord, bounds)
                rect = getTextRect(x, y, bounds)

                canvas.drawRoundRect(rect, labelBackgroundRadius, labelBackgroundRadius, shadowPaint)
                canvas.drawRoundRect(rect, labelBackgroundRadius, labelBackgroundRadius, labelBackgroundPaint)
                canvas.drawText(value, x, y, labelPaint)
            }
        }
    }

    private fun getTextRect(x: Float, y: Float, bounds: Rect): RectF {
        return RectF(
                x - LABEL_BACKGROUND_OFFSET,
                y - bounds.height() - LABEL_BACKGROUND_OFFSET,
                x + bounds.width() + LABEL_BACKGROUND_OFFSET,
                y + LABEL_BACKGROUND_OFFSET
        )
    }

    private fun getTextX(coord: Pair<Float, Float>, bounds: Rect): Float {
        return if (coord.first + bounds.width() + paddingEnd > canvas.width) {
            canvas.width - bounds.width().toFloat() - paddingEnd
        } else {
            coord.first
        }
    }

    private fun getTextY(coord: Pair<Float, Float>, bounds: Rect): Float {
        return if (coord.second - bounds.height() - paddingTop < 0) {
            bounds.height() + paddingTop.toFloat()
        } else {
            coord.second
        }
    }

    private fun invalidateScalePath() {
        val last = Math.min(points.size, MAX_ARM_COUNT)
        scalePath = Path().apply {
            var coord: Pair<Float, Float>
            var tmpArm: Int
            for (i in 1..SCALE_LINES_COUNT) {
                for (arm in 0..last) {
                    tmpArm = if (arm == last) 0 else arm
                    coord = getPointAtArm(tmpArm, i * scaleSpace)
                    if (arm == 0) {
                        moveTo(coord.first, coord.second)
                    } else {
                        lineTo(coord.first, coord.second)
                    }
                }
            }
        }
    }

    private fun invalidatePointsPath() {
        if (points.isNotEmpty()) {
            pointsPath = Path().apply {
                var coord: Pair<Float, Float>
                var point: Point
                val last = Math.min(points.size, MAX_ARM_COUNT)
                for (index in 0..last) {
                    point = if (index == last) points.first() else points[index]
                    coord = getPointAtArm(index, armLength * (point.value / point.maxValue.toFloat()))
                    if (index == 0) {
                        moveTo(coord.first, coord.second)
                    } else {
                        lineTo(coord.first, coord.second)
                    }
                }
            }
        }
    }

    private fun Path.drawArm(index: Int) {
        val point = getPointAtArm(index, armLength.toFloat())
        lineTo(point.first, point.second)
    }

    private fun getPointAtArm(index: Int, radius: Float): Pair<Float, Float> {
        val drawAngle = angle * index - 90
        val center = getCanvasCenter()
        return Pair(
                center.first + cos(Math.toRadians(drawAngle.toDouble())).toFloat() * radius,
                center.second + sin(Math.toRadians(drawAngle.toDouble())).toFloat() * radius
        )
    }

    private fun Path.moveToCenter() {
        with(getCanvasCenter()) {
            moveTo(first, second)
        }
    }

    private fun getCanvasCenter() = Pair(canvasBitmap.width / 2f, canvasBitmap.height / 2f)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(canvasBitmap, bitmapMatrix, bitmapPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(
                w - paddingStart - paddingStart,
                h - paddingBottom - paddingTop,
                Bitmap.Config.ARGB_8888
        )
        canvas = Canvas(canvasBitmap)
        bitmapMatrix.setRectToRect(
                RectF(0f, 0f, canvasBitmap.width.toFloat(), canvasBitmap.height.toFloat()),
                RectF(paddingLeft.toFloat(), paddingTop.toFloat(), w - paddingRight.toFloat(), h - paddingBottom.toFloat()),
                Matrix.ScaleToFit.START
        )
        redraw()
    }
}