package com.example.waveprogressbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.AttrRes
import androidx.core.animation.addListener
import java.lang.Float.max
import kotlin.math.min

class WaveProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_PROGRESS = 0
        private val DEFAULT_LIGHT_WAVE_COLOR = Color.parseColor("#1F5399FF")
        private val DEFAULT_DARK_WAVE_COLOR = Color.parseColor("#2E5399FF")
    }

    //波纹平移的执行的时间
    private var duration = 0L

    //波纹动画
    private var waveAnimator: ValueAnimator? = null

    //进度最大值
    private var max = DEFAULT_MAX

    //当前进度
    private var progress = DEFAULT_PROGRESS

    private var percent = 0f
    private var progressAnimator: ValueAnimator? = null

    //控件的宽度高度
    private var mWidth = 0
    private var mHeight = 0

    private var minHeight = 50f

    //浅色波浪
    private lateinit var lightWavePaint: Paint
    private lateinit var lightWavePath: Path
    private var lightWaveColor = 0
    private var lightWaveWidth = 0
    private var lightWaveHeight = 0
    private var lightWaveOffset = 0f

    //深色波浪
    private lateinit var darkWavePaint: Paint
    private lateinit var darkWavePath: Path
    private var darkWaveColor = 0
    private var darkWaveWidth = 0
    private var darkWaveHeight = 0
    private var darkWaveOffset = 0f

    init {
        attrs?.let {
            parseAttribute(getContext(), it)
        }
        initPaint()
    }

    //获取布局属性并设置属性默认值
    private fun parseAttribute(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressBar)
        duration = ta.getInt(R.styleable.WaveProgressBar_duration, 2000).toLong()
        max = ta.getInt(R.styleable.WaveProgressBar_waveMax, 100)
        progress = ta.getInt(R.styleable.WaveProgressBar_waveProgress, 30)
        lightWaveWidth = ta.getDimension(R.styleable.WaveProgressBar_lightWaveWidth, 800f).toInt()
        lightWaveHeight = ta.getDimension(R.styleable.WaveProgressBar_lightWaveHeight, 40f).toInt()
        darkWaveWidth = ta.getDimension(R.styleable.WaveProgressBar_darkWaveWidth, 800f).toInt()
        darkWaveHeight = ta.getDimension(R.styleable.WaveProgressBar_darkWaveHeight, 50f).toInt()
        darkWaveColor = ta.getColor(
            R.styleable.WaveProgressBar_darkWaveColor,
            DEFAULT_DARK_WAVE_COLOR
        )
        lightWaveColor = ta.getColor(
            R.styleable.WaveProgressBar_lightWaveColor,
            DEFAULT_LIGHT_WAVE_COLOR
        )
        max = correctMax(max)
        progress = correctProgress(progress)
        percent = progress * 1.0f / max
        ta.recycle()
    }

    //初始化画笔
    private fun initPaint() {
        lightWavePaint = Paint()
        lightWavePaint.isAntiAlias = true
        lightWavePaint.style = Paint.Style.FILL_AND_STROKE
        lightWavePaint.color = lightWaveColor
        lightWavePath = Path()
        darkWavePaint = Paint()
        darkWavePaint.isAntiAlias = true
        darkWavePaint.style = Paint.Style.FILL_AND_STROKE
        darkWavePaint.color = darkWaveColor
        darkWavePath = Path()
    }

    //重写onMeasure支持wrap_content
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
    }

    private fun measure(measureSpec: Int): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = 180
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth
        mHeight = measuredHeight
        // 开始动画
        startWaveAnimator()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //不断的计算波浪的路径
        calculatePath()
        canvas?.drawPath(lightWavePath, lightWavePaint)
        canvas?.drawPath(darkWavePath, darkWavePaint)
    }

    private fun calculatePath() {
        lightWavePath.reset()
        lightWavePath.moveTo(
            -lightWaveWidth + lightWaveOffset,
            mHeight * 1.0f - min(max(minHeight, mHeight * percent), mHeight - minHeight)
        )
        //绘制波浪
        for (i in -lightWaveWidth..mWidth + lightWaveWidth step lightWaveWidth) {
            //利用二阶贝塞尔曲线绘制
            lightWavePath.rQuadTo(lightWaveWidth / 4f, -1.0f * lightWaveHeight, lightWaveWidth / 2f, 0f)
            lightWavePath.rQuadTo(lightWaveWidth / 4f, lightWaveHeight * 1.0f, lightWaveWidth / 2f, 0f)
        }
        //绘制连线
        lightWavePath.lineTo(mWidth * 1.0f, mHeight * 1.0f)
        lightWavePath.lineTo(0f, mHeight * 1.0f)
        lightWavePath.close()

        darkWavePath.reset()
        darkWavePath.moveTo(
            -darkWaveWidth - darkWaveOffset,
            mHeight * 1.0f - min(max(minHeight, mHeight * percent), mHeight - minHeight)
        )
        //绘制波浪
        for (i in -darkWaveWidth..mWidth + darkWaveWidth step darkWaveWidth) {
            //利用二阶贝塞尔曲线绘制
            darkWavePath.rQuadTo(darkWaveWidth / 4f, -1.0f * darkWaveHeight, darkWaveWidth / 2f, 0f)
            darkWavePath.rQuadTo(darkWaveWidth / 4f, darkWaveHeight * 1.0f, darkWaveWidth / 2f, 0f)
        }
        //绘制连线
        darkWavePath.lineTo(mWidth * 1.0f, mHeight * 1.0f)
        darkWavePath.lineTo(0f, mHeight * 1.0f)
        darkWavePath.close()
    }

    //开始动画
    private fun startWaveAnimator() {
        if (waveAnimator != null && waveAnimator!!.isRunning) {
            return
        }
        waveAnimator = ValueAnimator.ofFloat(0f, 1f)
        waveAnimator!!.duration = duration
        waveAnimator!!.repeatCount = ValueAnimator.INFINITE
        waveAnimator!!.interpolator = LinearInterpolator()
        waveAnimator!!.addUpdateListener { animation ->
            lightWaveOffset = (animation.animatedValue as Float) * lightWaveWidth
            darkWaveOffset = (animation.animatedValue as Float) * darkWaveWidth
            postInvalidate()
        }
        waveAnimator!!.addListener(onStart = {
            lightWaveOffset = 0f
            darkWaveOffset = 0f
        })
        waveAnimator!!.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimator()
    }

    private fun stopAnimator() {
        if (waveAnimator != null && waveAnimator!!.isRunning) {
            waveAnimator!!.cancel()
            waveAnimator!!.removeAllUpdateListeners()
            waveAnimator = null
        }
        if (progressAnimator != null && progressAnimator!!.isRunning) {
            progressAnimator!!.cancel()
            progressAnimator!!.removeAllUpdateListeners()
            progressAnimator = null
        }
    }

    fun setProgress(progress: Int, anim: Boolean = true) {
        this.progress = correctProgress(progress)
        if (anim) {
            val start = percent
            val end = progress * 1.0f / max
            startAnimator(start, end)
        } else {
            percent = progress * 1.0f / max
            postInvalidate()
        }
    }

    fun setMaxValue(max: Int) {
        this.max = correctMax(max)
        postInvalidate()
    }

    private fun correctMax(max: Int): Int {
        return if (max == 0) {
            1
        } else {
            max
        }
    }

    private fun correctProgress(progress: Int): Int {
        var progress = progress
        if (progress > max) {  //处理错误输入progress大于max的情况
            if (progress % max == 0) {
                progress = max
            } else {
                progress %= max
            }
        }
        return progress
    }

    private fun startAnimator(start: Float, end: Float) {
        if (start == end) {
            return
        }
        progressAnimator = ValueAnimator.ofFloat(start, end)
        progressAnimator!!.duration = duration
        progressAnimator!!.addUpdateListener { animation ->
            percent = animation.animatedValue as Float
            postInvalidate()
        }
        progressAnimator!!.start()
    }
}