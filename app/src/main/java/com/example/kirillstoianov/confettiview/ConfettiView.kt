package com.hily.app.presentation.ui.views.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.kirillstoianov.confettiview.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


/**
 * Created by Kirill Stoianov on 07.11.18.
 *
 * @link http://android-er.blogspot.com/2014/05/draw-star-on-canvas.html
 * @link http://android-er.blogspot.com/2014/05/draw-path-of-polygon-on-canvas-of.html
 * @link https://stackoverflow.com/questions/3630086/how-to-get-string-width-on-android
 */
class ConfettiView(context: Context, attributeSet: AttributeSet?, def: Int) : View(context, attributeSet, def) {

    /**
     * Finish view animation listener.
     *
     * Set this finishListener outside from this view
     * for handle animation finish event.
     * */
    public var finishListener: () -> Unit = {}

    private var isViewAnimated: Boolean = false


    private val DEFAULT_BACKGROUND_ALPHA = 0
    private val DEFAULE_TITLE_TEXT_APLHA = 0
    private val DEFAULT_TITLE_Y_POSITION = 0f
    private val DEFAULT_CONFETTI_RADIUS = 0f
    private val DEFAULT_HAND_DEGREE = -15f
    private val DEFAULT_HAND_WIDTH = 0f

    //CONFETTI
    /**
     * List with confetti shapes.
     */
    private var confettiItems: ArrayList<ConfettiShape> = ArrayList()

    /**
     * List with colors for confetti shapes.
     */
    private val confettiColors: ArrayList<Int> = arrayListOf(
        Color.parseColor("#ff0096"), Color.parseColor("#fe692e"),
        Color.parseColor("#50e3c2"), Color.parseColor("#ffb300"),
        Color.parseColor("#ff415c"), Color.parseColor("#ff415c"),
        Color.parseColor("#04d95c"), Color.parseColor("#135bfe")
    )

    //HAND
    /**
     * Bitmap with hand.
     */
    private val handBitmap: Bitmap by lazy {
        val source = BitmapFactory.decodeResource(resources, R.drawable.img_sayhi_hand)
        val bitmapWidth = getHandBitmapWidth()
        val bitmapHeight = getHandBitmapHeight()
        return@lazy Bitmap.createScaledBitmap(source, bitmapWidth, bitmapHeight, true)
    }

    /**
     * Value which need multiply on [getHandBitmapWidth]
     * for get [handBitmap] height.
     */
    private val handBitmapHeightRatio = 1.3f


    //TITLE
    /**
     * Title text.
     */
    private val titleText: String = "You said “Hi!”"

    /**
     * Title text bounds for calculate
     * text position on canvas.
     */
    private val titleTextBound = Rect()

    /**
     * Title text paint.
     */
    private val titleTextPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
            textSize = height / 12f
            isAntiAlias = true

            //Set text shadow only when text alpha animation finished
            //in draw drawTitle()
            /*
            val shadowRadiusDpSize = 4
            val scaledShadowSizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                shadowRadiusDpSize.toFloat(),
                context.resources.displayMetrics
            )
            setShadowLayer(scaledShadowSizeInPx, 0f, 4f, Color.parseColor("#80000000"))
            */
        }
    }

    /**
     * Title text alpha.
     * This value animated by [titleTextAlphaAnimator].
     */
    private var animatedTitleTextAlpha: Int = DEFAULE_TITLE_TEXT_APLHA

    /**
     * Start title text alpha (transparent)
     */
    private val startTitleTextAlpha: Int = DEFAULE_TITLE_TEXT_APLHA


    //BACKGROUND
    private val startBackgroundAlpha = DEFAULT_BACKGROUND_ALPHA
    private val finishBackgroundAlpha: Int = 255 / 2
    private val backgroundPaint by lazy {
        Paint().apply {
            color = Color.BLACK
            alpha = animatedBackgroundAlpha
        }
    }

    //ANIMATED VALUES
    /**
     * Background paint for draw gradient on canvas.
     *
     * Receive values from 0.. 255 where 0 - it is transparent
     * and 255 non transparent.
     */
    private var animatedBackgroundAlpha: Int = DEFAULT_BACKGROUND_ALPHA //transparent on start

    /**
     * Finish title text alpha after animation (non transparent).
     */
    private val finishTitleTextAlpha: Int = 255

    /**
     * Title text position by y-axis.
     * This value animated by [titleTextTransitionAnimator].
     */
    private var animatedTitleYPosition = DEFAULT_TITLE_Y_POSITION

    /**
     * Radius of imaginary circle
     * which increase with [confettiDistanceAnimator].
     *
     * Need for animate [ConfettiShape]'s transition from center
     * to destination position.
     */
    private var animatedConfettiRadius: Float = DEFAULT_CONFETTI_RADIUS

    /**
     * Rotate degree anim value if [handBitmap].
     *
     * Need for animate 'say hi' gesture.
     */
    private var animatedHandDegree: Float = DEFAULT_HAND_DEGREE

    /**
     * Scale anim value of [handBitmap].
     *
     * Need for animate [handBitmap] showing effect.
     */
    private var animatedHandWidth: Float = DEFAULT_HAND_WIDTH

    /**
     * Matrix for draw [handBitmap] on [Canvas]
     * with: 'translate','rotate' and 'scale' effects.
     */
    private val handMatrix = Matrix()

    /**
     * Background alpha animator.
     */
    private val backgroundAlphaAnimator by lazy {
        ValueAnimator.ofInt(startBackgroundAlpha, finishBackgroundAlpha).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addUpdateListener {
                animatedBackgroundAlpha = it.animatedValue as Int
                invalidate()
            }
        }
    }

    /**
     * Animator of [animatedTitleTextAlpha] for animate title
     * text alpha.
     */
    private val titleTextAlphaAnimator by lazy {
        ValueAnimator.ofInt(startTitleTextAlpha, finishTitleTextAlpha).apply {
            duration = 300
            addUpdateListener {
                animatedTitleTextAlpha = it.animatedValue as Int
                invalidate()
            }
        }
    }

    /**
     * Animator of [animatedTitleYPosition] for animate
     * title text transition.
     */
    private val titleTextTransitionAnimator by lazy {
        titleTextPaint.getTextBounds(titleText, 0, titleText.length, titleTextBound)
        val startPositionByY = height / 5f + titleTextBound.height() * 2
        val finishPositionByY = height / 5f
        return@lazy ValueAnimator.ofFloat(startPositionByY, finishPositionByY).apply {
            addUpdateListener {
                duration = 300
                animatedTitleYPosition = it.animatedValue as Float
                invalidate()
            }
        }
    }


    /**
     * Animator of [animatedConfettiRadius] for animate [ConfettiShape]'s
     * transition effect.
     */
    private val confettiDistanceAnimator by lazy {
        ValueAnimator.ofFloat(0f, Math.min(width, height).toFloat() /2)
            .apply {
//                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                duration = 3000
                addUpdateListener {
                    animatedConfettiRadius = it.animatedValue as Float
                    invalidate()
                }
            }
    }

    /**
     * Animator of [handBitmap] rotation.
     */
    private val handDegreeAnimator by lazy {
        ValueAnimator.ofFloat(-15f, 15f).apply {
            startDelay = 100
            duration = 450
            repeatCount = 2
            repeatMode = ValueAnimator.REVERSE
        }
    }

    /**
     * Animator of [handBitmap] scaling effect.
     */
    private val handScaleAnimator by lazy {
        ValueAnimator.ofFloat(0f, getHandBitmapWidth().toFloat()).apply {
            startDelay = 100
            duration = 300
            addUpdateListener {
                animatedHandWidth = it.animatedValue as Float
                invalidate()
            }
        }
    }

    /**
     * Initialize block
     */
    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        post {
            generateConfetti()
        }
    }


    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isViewAnimated.not()) return

        canvas?.apply {

            //draw gradient background
            drawGradientBackground(this)

            //draw confetti items
            confettiItems.forEach { shape ->
                drawConfetti(this, shape)
            }

            //draw hand
            drawHand(this)

            //draw title text
            drawTitle(this)
        }
    }

    /**
     * Start view animation.
     */
    fun startAnimate() {
        clearView()
        isViewAnimated = true

        /*
        * NOTE: hand degree animation is more longer than other
        * and call finishListener.
        * Set listeners before all animation starts
        * and remove when animation is finished.*/
        val updateListener = object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                animatedHandDegree = animation.animatedValue as Float
                invalidate()
            }
        }
        handDegreeAnimator.addUpdateListener(updateListener)

        val mainAnimationListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {

                //remove listener when main animation finished
                handDegreeAnimator?.removeListener(this)
                handDegreeAnimator?.removeUpdateListener(updateListener)
                handDegreeAnimator?.removeAllListeners()
                handDegreeAnimator?.removeAllUpdateListeners()

                //invoke finish callback
                finishListener.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        }
        handDegreeAnimator.addListener(mainAnimationListener)

        titleTextAlphaAnimator.start()
        titleTextTransitionAnimator.start()
        backgroundAlphaAnimator.start()
        handScaleAnimator.start()
        confettiDistanceAnimator.start()
        handDegreeAnimator.start()
    }

    fun clearView() {
        isViewAnimated = false

        animatedTitleTextAlpha = DEFAULE_TITLE_TEXT_APLHA
        animatedTitleYPosition = DEFAULT_TITLE_Y_POSITION
        animatedBackgroundAlpha = DEFAULT_BACKGROUND_ALPHA
        animatedConfettiRadius = DEFAULT_CONFETTI_RADIUS
        animatedHandDegree = DEFAULT_HAND_DEGREE
        animatedHandWidth = DEFAULT_HAND_WIDTH

        if (titleTextAlphaAnimator.isRunning) {
            titleTextAlphaAnimator.cancel()
            titleTextAlphaAnimator.end()
        }

        if (titleTextTransitionAnimator.isRunning) {
            titleTextTransitionAnimator.cancel()
            titleTextTransitionAnimator.end()
        }

        if (backgroundAlphaAnimator.isRunning) {
            backgroundAlphaAnimator.cancel()
            backgroundAlphaAnimator.end()
        }

        if (handScaleAnimator.isRunning) {
            handScaleAnimator.cancel()
            handScaleAnimator.end()
        }

        if (confettiDistanceAnimator.isRunning) {
            confettiDistanceAnimator.cancel()
            confettiDistanceAnimator.end()
        }

        if (handDegreeAnimator.isRunning) {
            handDegreeAnimator.cancel()
            handDegreeAnimator.end()
        }

        handDegreeAnimator.removeAllListeners()
        handDegreeAnimator.removeAllUpdateListeners()

        invalidate()
    }

    /**
     * Draw view gradient background.
     */
    private fun drawGradientBackground(canvas: Canvas) {
        backgroundPaint.alpha = animatedBackgroundAlpha
        canvas.drawPaint(backgroundPaint)
    }

    /**
     * Draw confetti shape.
     *
     */
    private fun drawConfetti(canvas: Canvas, shape: ConfettiShape) {
        //get shape position on confetti imaginary circle by x-asix
        shape.pX = width / 2 +
                ((animatedConfettiRadius + shape.drawConfig.radiusDeviation) * Math.cos(Math.toRadians(shape.drawConfig.angleDeviation.toDouble())).toFloat())

        //get shape position on confetti imaginary circle by y-asix
        shape.pY = height / 2 +
                ((animatedConfettiRadius + shape.drawConfig.radiusDeviation) * Math.sin(Math.toRadians(shape.drawConfig.angleDeviation.toDouble())).toFloat())

        //get shape radius
        shape.radius = getShapeRadius(shape)

        shape.draw(canvas)


        ///////////////
        shape.drawConfig.angleDeviation=shape.drawConfig.angleDeviation+0.5f
    }

    /**
     * Draw view title text.
     */
    private fun drawTitle(canvas: Canvas) {

        //get text bounds for title text and his Paint.
        titleTextPaint.getTextBounds(titleText, 0, titleText.length, titleTextBound)
        titleTextPaint.alpha = animatedTitleTextAlpha

        //set shadow only when text alpha animation is finished
        if (animatedTitleTextAlpha == finishTitleTextAlpha) {
            val shadowRadiusDpSize = 4
            val scaledShadowSizeInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                shadowRadiusDpSize.toFloat(),
                context.resources.displayMetrics
            )
            titleTextPaint.setShadowLayer(scaledShadowSizeInPx, 0f, 4f, Color.parseColor("#80000000"))
        } else {
            titleTextPaint.setShadowLayer(0f, 0f, 4f, Color.TRANSPARENT)
        }

        val textWidth = titleTextBound.width()

        val textLeft = width / 2f - (textWidth / 2)
        val textTop = animatedTitleYPosition
        canvas.drawText(titleText, textLeft, textTop, titleTextPaint)
    }

    /**
     * Draw [handBitmap].
     */
    private fun drawHand(canvas: Canvas) {

        handMatrix.reset()

        //get scale factor
        val scaleWidthFactor = animatedHandWidth / handBitmap.width
        val scaleHeightFactor = animatedHandWidth * handBitmapHeightRatio / handBitmap.height

        //calculate new width/height
        val newWidth = handBitmap.width * scaleWidthFactor
        val newHeight = handBitmap.height * scaleHeightFactor

        //calculate start position by x-axis and y-axis
        val handLeft = (width / 2f) - newWidth / 2
        val handTop = (height / 2f) - newHeight / 2

        //apply transformation for bitmap
        handMatrix.preRotate(
            animatedHandDegree,
            newWidth / 2f/*point of rotation by x-axis*/,
            newHeight/*point of rotate by y-axis*/
        )
        handMatrix.postScale(scaleWidthFactor, scaleHeightFactor)
        handMatrix.postTranslate(handLeft, handTop)

        canvas.drawBitmap(handBitmap, handMatrix, null)
    }

    /**
     * Get shape radius for drawing.
     *
     * [ConfettiView] support 3 different size's for [ConfettiShape].
     */
    private fun getShapeRadius(confettiShape: ConfettiShape): Float {
        val diameter = when (confettiShape.drawConfig.sizeType) {
            ConfettiShape.DrawConfig.SizeType.SMALL -> Math.min(width, height) / 36f
            ConfettiShape.DrawConfig.SizeType.MEDIUM -> Math.min(width, height) / 27f
            ConfettiShape.DrawConfig.SizeType.LARGE -> Math.min(width, height) / 17f
        }
        return diameter / 2
    }

    /**
     * Fill local list with random [ConfettiShape]'s.
     */
    private fun generateConfetti() {
        val count = 60
        IntRange(0, count).forEach { index ->
            confettiItems.add(getRandomShape(count, index))
        }
    }

    /**
     * Get random [ConfettiShape] item with random configuration.
     */
    private fun getRandomShape(count: Int, index: Int): ConfettiShape {

        val drawConfig =
            ConfettiShape.DrawConfig(
                getRandomShapeType(),
                getRandomShapeSize(),
//                        getRandomDegree().toFloat(),
                getDegreeRalatedToCount(count, index),
                getRandomRadiusOffset()
            )

        return ConfettiShape.Builder()
            .setDrawConfig(drawConfig)
            .setColor(getRandomColor())
            .build()
    }


    /**
     * Get random degree for [ConfettiShape]
     * on imaginary circle.
     *
     * @return - values in range 0.. 360.
     *
     * @see [getDegreeRalatedToCount]
     */
    private fun getRandomDegree(): Int {
        return Random().nextInt(180) * 2//(0 until 180).random() * 2
    }

    /**
     * Get degree for [ConfettiShape]
     * related to all shapes count and current item index.
     * @return values in range 0.. 360
     * @see [getRandomDegree]
     */
    private fun getDegreeRalatedToCount(count: Int, index: Int): Float {
        val anglePerShape: Float = 360f / count
        return index * anglePerShape
    }

    /**
     * Get random value which wil attach to
     * [animatedConfettiRadius] for make different radius
     * of imaginary circle for each [ConfettiShape].
     */
    private fun getRandomRadiusOffset(): Int {
        if (width == 0) return 0
        return Random().nextInt(width / 5)//(0 until (width / 5)).random()
    }

    /**
     * Get random shape size.
     */
    private fun getRandomShapeSize(): ConfettiShape.DrawConfig.SizeType {
        val random = Random().nextInt(4)//(0 until 4).random()
        return when (random) {
            1 -> ConfettiShape.DrawConfig.SizeType.SMALL
            2 -> ConfettiShape.DrawConfig.SizeType.MEDIUM
            3 -> ConfettiShape.DrawConfig.SizeType.LARGE
            else -> ConfettiShape.DrawConfig.SizeType.MEDIUM
        }
    }

    /**
     * Get random color for each [ConfettiShape].
     */
    private fun getRandomColor(): Int {
        val random = Random().nextInt(confettiColors.size)// (0 until confettiColors.size).random()
        return confettiColors[random]
    }

    /**
     * Get random shape type.
     */
    private fun getRandomShapeType(): ConfettiShape.DrawConfig.ShapeType {
        val random = Random().nextInt(5)//(0 until 5).random()
        return when (random) {
            1 -> ConfettiShape.DrawConfig.ShapeType.CIRCLE
            2 -> ConfettiShape.DrawConfig.ShapeType.RECT
            3 -> ConfettiShape.DrawConfig.ShapeType.PENTAGON
            4 -> ConfettiShape.DrawConfig.ShapeType.STAR
            else -> ConfettiShape.DrawConfig.ShapeType.RECT
        }
    }

    /**
     * Get [handBitmap] width.
     */
    private fun getHandBitmapWidth(): Int {
        if (width == 0 || height == 0) return 0
        return Math.min(width, height) / 3
    }

    /**
     * Get [handBitmap] height.
     */
    private fun getHandBitmapHeight(): Int {
        if (width == 0 || height == 0) return 0
        return (getHandBitmapWidth() * handBitmapHeightRatio).roundToInt()
    }

    /**
     * Confetti shape representation.
     */
    class ConfettiShape {

        class Builder {
            private val confettiShape = ConfettiShape()

            fun setDrawConfig(drawConfig: DrawConfig): Builder {
                confettiShape.drawConfig = drawConfig
                return this
            }

            fun setColor(color: Int): Builder {
                confettiShape.setColor(color)
                return this
            }

            fun build(): ConfettiShape = confettiShape
        }

        /**
         * Base and additional drawing configuration.
         *
         * Contains base info about shape as [shapeType] and [sizeType]
         * and additional params as [angleDeviation] and [radiusDeviation]
         * which need for randomize [ConfettiShape] drawing position.
         */
        class DrawConfig(

            /**
             * Shape type
             */
            var shapeType: ShapeType = ShapeType.CIRCLE,

            /**
             * Shape size type.
             */
            var sizeType: SizeType = SizeType.MEDIUM,

            /**
             * Angle on confetti imaginary circle.
             *
             * NOTE: it deviation from '0 degree'.
             */
            var angleDeviation: Float = 0f,

            /**
             * Deviation of confetti imaginary circle radius.
             */
            var radiusDeviation: Int = 0
        ) {
            enum class ShapeType { CIRCLE, RECT, PENTAGON, STAR }
            enum class SizeType { SMALL, MEDIUM, LARGE }
        }

        /**
         * Paint of shape.
         */
        val paint: Paint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.GREEN
            isAntiAlias = true
        }

        /**
         * Path of shape.
         */
        val path: Path = Path()

        /**
         * Position of shape by x-axis
         */
        var pX: Float = 0f

        /**
         * Position of shape by y-axis
         */
        var pY: Float = 0f

        /**
         * Radius of shape.
         */
        var radius: Float = 0f

        /**
         * Additional drawing params.
         */
        var drawConfig = DrawConfig()

        private fun setCircle(x: Float, y: Float, radius: Float, dir: Path.Direction) {
            path.reset()
            path.addCircle(x, y, radius, dir)
        }

        private fun setPolygon(x: Float, y: Float, radius: Float, numOfPt: Int) {

            val section = 2.0 * Math.PI / numOfPt

            path.reset()
            path.moveTo(
                (x + radius * Math.cos(0.0)).toFloat(),
                (y + radius * Math.sin(0.0)).toFloat()
            )

            for (i in 1 until numOfPt) {
                path.lineTo(
                    (x + radius * Math.cos(section * i)).toFloat(),
                    (y + radius * Math.sin(section * i)).toFloat()
                )
            }

            path.close()
        }

        private fun setStar(x: Float, y: Float, radius: Float, innerRadius: Float, numOfPt: Int) {

            val section = 2.0 * Math.PI / numOfPt

            path.reset()
            path.moveTo(
                (x + radius * Math.cos(0.0)).toFloat(),
                (y + radius * Math.sin(0.0)).toFloat()
            )
            path.lineTo(
                (x + innerRadius * Math.cos(0 + section / 2.0)).toFloat(),
                (y + innerRadius * Math.sin(0 + section / 2.0)).toFloat()
            )

            for (i in 1 until numOfPt) {
                path.lineTo(
                    (x + radius * Math.cos(section * i)).toFloat(),
                    (y + radius * Math.sin(section * i)).toFloat()
                )
                path.lineTo(
                    (x + innerRadius * Math.cos(section * i + section / 2.0)).toFloat(),
                    (y + innerRadius * Math.sin(section * i + section / 2.0)).toFloat()
                )
            }

            path.close()
        }

        /**
         * Set color for current [ConfettiShape].
         */
        fun setColor(color: Int) {
            paint.color = color
        }

        /**
         * Draw current shape with current params on canvas.
         */
        fun draw(canvas: Canvas) {
            when (drawConfig.shapeType) {
                DrawConfig.ShapeType.CIRCLE -> {
                    setCircle(pX, pY, radius, Path.Direction.CCW)
                    canvas.drawPath(path, paint)
                }
                DrawConfig.ShapeType.RECT -> {
                    setPolygon(pX, pY, radius, 4)
                    canvas.drawPath(path, paint)
                }
                DrawConfig.ShapeType.PENTAGON -> {
                    setPolygon(pX, pY, radius, 5)
                    canvas.drawPath(path, paint)
                }
                DrawConfig.ShapeType.STAR -> {
                    setStar(pX, pY, radius, radius / 2, 5)
                    canvas.drawPath(path, paint)
                }
                else -> throw UnsupportedOperationException("Unsupported type for draw: [${drawConfig.sizeType}]")
            }
        }
    }
}