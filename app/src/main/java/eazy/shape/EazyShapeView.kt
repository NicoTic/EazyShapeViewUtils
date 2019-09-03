package eazy.shape

import android.content.Context
import android.graphics.*
import android.text.*
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import createStaticLayout
import withTranslation
import java.util.*
import kotlin.math.floor
import kotlin.math.sqrt

/**
 *
 * 类名： eazy.shape
 * 描述：
 * 日期：2019/9/2 :34
 * @author  jiangxq
 */
class EazyShapeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr){
    companion object{
        private val DEFAULT_SIZE = 100f
    }
    var defaultSize: kotlin.Int = 0

    var shapeSize: Float =10f
    var shapeRoundRectSize: Float = 0f
    var shapeStrokeWidth: Float= 0f
    var shapeColor:Int = 0

    //画笔
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var oval = RectF()

    //圆角位置
    var shapeRadiusPos = 0
    //实心还是空心
    var isStrokeOrFill = false
    //轮廓为实线/虚线
    var isSolidOrDotted = true

    var corners : FloatArray = FloatArray(8)

    //虚线相关
    var dottedArray = FloatArray(4)
    var dashPathEffect = PathEffect()
    var dottedLength = 5f
    var whiteLength = 2f

    //透明度
    var shapeAlpha = 0

    val path = Path()

    //文字相关
    var centerTextSize = 0f
    var text:String ?= ""
    var centerTextColor = 0
    var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    //场景一：可直接设置文字大小，文字大小与Padding无关，该文字将直接在View中间显示，
    //超过一定宽度则换行,超过一定长度则显示...

    var preview: SpannableString? = null
    var textWidth = 0f
    var textHeight = 0f
    var staticLayout: StaticLayout? = null
    var textLength = 0f
    var textType = 0

    //场景二,适用于多行文字的完整展示，你不需要为其设置字体大小，字体自适应
    var mWidth = 0
    var mHeight = 0
    var mTextBreakPoints: MutableList<Int>? = null
    var multiLineTextComplete = false


    init {
        initDimens()
        initAttrs(attrs)
        initView()
    }

    private fun initView() {
        dashPathEffect = DashPathEffect(dottedArray,0f)

        paint.apply {
            style = if(isStrokeOrFill) Paint.Style.STROKE else Paint.Style.FILL
            color = shapeColor
            isAntiAlias = true
            strokeWidth = shapeStrokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            if(!isSolidOrDotted) pathEffect = dashPathEffect
            alpha = shapeAlpha
        }

        textPaint.apply {
            textSize = centerTextSize
            color = centerTextColor
        }
    }

    private fun setSuitableTextSize() {
        var textSize = getEstimateTextSize()
        while (textSize > 0) {
            if (isTextSizeSuitable(textSize))
                return
            textSize--
        }
    }

    private fun getEstimateTextSize(): Int {
        return sqrt((mWidth * mHeight / text?.length!! * 2.0).toDouble()).toInt()
    }

    private fun isTextSizeSuitable(size: Int): Boolean {
        mTextBreakPoints = ArrayList()
        textPaint.textSize = size.toFloat()
        var start = 0
        val end = text!!.length
        while (start < end) {
            val len = textPaint.breakText(
                    text, start, end, true, mWidth.toFloat(),
                    null
            )
            start += len
            (mTextBreakPoints as ArrayList<Int>).add(start)
        }
        return (mTextBreakPoints as ArrayList<Int>).size * size < mHeight
    }

    private fun initDimens() {
        dottedArray = floatArrayOf(
                dottedLength,whiteLength,10f,8f
        )
        val density = context.resources.displayMetrics.density
        this.defaultSize = (DEFAULT_SIZE * density).toInt()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        context!!.theme.obtainStyledAttributes(
                attrs,
                eazy.R.styleable.EazyShapeView,
                0, 0).apply {
            try {
                shapeSize = getDimension(eazy.R.styleable.EazyShapeView_shapeSize, defaultSize.toFloat())
                shapeStrokeWidth = getDimension(eazy.R.styleable.EazyShapeView_shapeStrokeWidth, 0f)
                val defShapeColor = Color.BLACK
                shapeColor = getColor(eazy.R.styleable.EazyShapeView_shapeColor,defShapeColor)
                shapeRoundRectSize = getDimension(eazy.R.styleable.EazyShapeView_shapeRoundRectSize,0f)
                shapeRadiusPos = getInteger(eazy.R.styleable.EazyShapeView_shapeRadiusPosition,-1)
                isStrokeOrFill = getBoolean(eazy.R.styleable.EazyShapeView_isStrokeOrFill,false)
                isSolidOrDotted = getBoolean(eazy.R.styleable.EazyShapeView_isSolidOrDotted,true)
                shapeAlpha = getInteger(eazy.R.styleable.EazyShapeView_shapeAlpha,255)
                centerTextSize = getDimension(eazy.R.styleable.EazyShapeView_textSize,18.0f)
                centerTextColor = getColor(eazy.R.styleable.EazyShapeView_textColor, Color.BLACK)
                text = getString(eazy.R.styleable.EazyShapeView_textString)
                textType = getInteger(eazy.R.styleable.EazyShapeView_textType,0)
                multiLineTextComplete = getBoolean(eazy.R.styleable.EazyShapeView_multi_line_complete,false)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        var width = 0
        var height = 0

        when (widthSpec) {
            MeasureSpec.AT_MOST -> width = defaultSize
            MeasureSpec.EXACTLY -> width = measuredWidth
        }

        when (heightSpec) {
            MeasureSpec.AT_MOST -> height = defaultSize
            MeasureSpec.EXACTLY -> height = measuredHeight
        }

        val strokeHalf = shapeStrokeWidth / 2

        oval.set(strokeHalf,
                strokeHalf,
                width-strokeHalf,
                height-strokeHalf)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when(shapeRadiusPos){
            -1 -> createCorner()
            0 -> createCornerAll(shapeRoundRectSize)
            1 -> createCornerLeft(shapeRoundRectSize)
            2 -> createCornerTop(shapeRoundRectSize)
            3 -> createCornerRight(shapeRoundRectSize)
            4 -> createCornerBottom(shapeRoundRectSize)
            5 -> createCornerTopLeft(shapeRoundRectSize)
            6 -> createCornerTopRight(shapeRoundRectSize)
            7 -> createCornerBottomLeft(shapeRoundRectSize)
            8 -> createCornerBottomRight(shapeRoundRectSize)
            9 -> createCornerTopLeftBottomRight(shapeRoundRectSize)
            10 -> createCornerTopRightBottomLeft(shapeRoundRectSize)
        }

        path.addRoundRect(oval, corners, Path.Direction.CW)
        canvas?.apply {
            drawEazyShape()
            if(multiLineTextComplete){
                drawPaddingCenterText()
            }else{
                drawCenterText()
            }
        }
    }

    private fun Canvas.drawPaddingCenterText() {
        mWidth = width-paddingLeft-paddingRight
        mHeight = height-paddingTop-paddingBottom
        setSuitableTextSize()
        var start = 0
        val x = paddingLeft
        var y = paddingTop
        for (point in mTextBreakPoints!!) {
            y += textPaint.getTextSize().toInt()
            text?.let { this.drawText(it, start, point, x.toFloat(), y.toFloat(), textPaint) }
            start = point
        }
    }

    private fun createCornerTopRightBottomLeft(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                radius,radius, // Top right radius in px
                0f,0f, // Bottom right radius in px
                radius,radius  // Bottom left radius in px
        )

    }

    private fun createCornerTopLeftBottomRight(radius: Float) {
        corners = floatArrayOf(
                radius,radius, // Top left radius in px
                0f,0f, // Top right radius in px
                radius,radius, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )

    }

    private fun Canvas.drawCenterText() {
        val bounds = this.getClipBounds()

        if(TextUtils.isEmpty(text)){
            text = ""
        }

        preview = SpannableString(text)
        val styleSpan = when(textType){
            1-> StyleSpan(Typeface.BOLD)
            2-> StyleSpan(Typeface.ITALIC)
            else-> StyleSpan(Typeface.NORMAL)
        }
        text?.length?.let {
            preview!!.setSpan(styleSpan,0,
                    it, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        textWidth = (width-paddingLeft-paddingRight).toFloat()
        textHeight = (height-paddingTop-paddingBottom).toFloat()

        val textMetrics = textPaint.getFontMetrics()
        val distance = textMetrics.bottom - textMetrics.top
        textLength = textPaint.measureText(text)

        staticLayout = createStaticLayout(
                source = preview!!,
                paint = textPaint,
                textWidth = textWidth.toInt(),
                maxLines = floor((textHeight/distance).toDouble()).toInt()
        )
        val centerX = width/2.0f
        val centerY = bounds.exactCenterY() - staticLayout!!.lineCount * distance / 2.0f

        //设置了左边距、右边距、上边距和下边距的话则按照左边距，则在边距的位置展示
        //若没有设置以上信息，则默认中间显示
        if(paddingLeft<=0f||paddingRight<=0f||paddingTop<=0f||paddingBottom<=0f){
            textPaint.textAlign = Paint.Align.CENTER
            this.save()
            this.withTranslation (centerX,centerY){
                staticLayout!!.draw(this)
            }
            this.restore()
        }else{
            textPaint.textAlign = Paint.Align.LEFT
            this.save()
            this.withTranslation (paddingLeft.toFloat(),paddingTop.toFloat()){
                staticLayout!!.draw(this)
            }
            this.restore()
        }

        //text?.let { drawText(it,centerX,centerY,textPaint) }
    }

    private fun createCornerAll(radius: Float) {
        corners = floatArrayOf(
                radius,radius, // Top left radius in px
                radius,radius, // Top right radius in px
                radius,radius, // Bottom right radius in px
                radius,radius  // Bottom left radius in px
        )
    }

    private fun createCornerBottomRight(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                0f,0f, // Top right radius in px
                radius,radius, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    private fun createCornerBottomLeft(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                0f,0f, // Top right radius in px
                0f,0f, // Bottom right radius in px
                radius,radius  // Bottom left radius in px
        )
    }

    private fun createCornerTopRight(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                radius,radius, // Top right radius in px
                0f,0f, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    private fun createCornerTopLeft(radius: Float) {
        corners = floatArrayOf(
                radius,radius, // Top left radius in px
                0f,0f, // Top right radius in px
                0f,0f, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    private fun createCornerBottom(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                0f,0f, // Top right radius in px
                radius,radius, // Bottom right radius in px
                radius,radius  // Bottom left radius in px
        )
    }

    private fun createCornerRight(radius: Float) {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                radius,radius, // Top right radius in px
                radius,radius, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    private fun createCornerTop(radius: Float) {
        corners = floatArrayOf(
                radius,radius, // Top left radius in px
                radius,radius, // Top right radius in px
                0f,0f, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    private fun createCornerLeft(radius: Float) {
        corners = floatArrayOf(
                radius,radius, // Top left radius in px
                0f,0f, // Top right radius in px
                0f,0f, // Bottom right radius in px
                radius,radius  // Bottom left radius in px
        )

    }

    private fun createCorner() {
        corners = floatArrayOf(
                0f,0f, // Top left radius in px
                0f,0f, // Top right radius in px
                0f,0f, // Bottom right radius in px
                0f,0f  // Bottom left radius in px
        )
    }

    /**
     * 绘制简单的Shape
     */
    private fun Canvas.drawEazyShape() {
        drawPath(path,paint)
    }
}