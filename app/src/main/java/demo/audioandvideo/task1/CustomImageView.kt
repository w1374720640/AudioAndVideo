package demo.audioandvideo.task1

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 *
 * Created by wang on 2021/10/23.
 */
class CustomImageView : View {
    private var bitmap: Bitmap? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, style: Int) : super(
        context,
        attributeSet,
        style
    )

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            // 根据比例计算能完整显示Bitmap的最大尺寸
            val ratio = it.width.toFloat() / it.height
            var dstWidth = 0f
            var dstHeight = 0f
            if (width.toFloat() / height > ratio) {
                dstHeight = height.toFloat()
                dstWidth = dstHeight * ratio
            } else {
                dstWidth = width.toFloat()
                dstHeight = dstWidth / ratio
            }
            canvas.drawBitmap(
                it, Rect(0, 0, it.width, it.height),
                RectF(0f, 0f, dstWidth, dstHeight), null
            )
        }

    }
}