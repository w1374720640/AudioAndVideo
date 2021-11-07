package demo.audioandvideo.task1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import demo.audioandvideo.R
import demo.audioandvideo.utils.ToastUtils
import java.io.IOException

/**
 *
 * Created by wang on 2021/10/20.
 */
class DrawImageActivity : AppCompatActivity() {
    private val fileName = "image/Android11Logo.png"
    private lateinit var imageViewSrc: ImageView
    private lateinit var imageViewBg: ImageView
    private lateinit var surfaceView: SurfaceView
    private lateinit var textureView: TextureView
    private lateinit var customView: CustomImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_image)

        initView()
        val bitmap = readFileFromAssets()
        if (bitmap != null) {
            setData(bitmap)
        }
    }

    private fun initView() {
        imageViewSrc = findViewById(R.id.imageSrc)
        imageViewBg = findViewById(R.id.imageBg)
        surfaceView = findViewById(R.id.surfaceView)
        textureView = findViewById(R.id.textureView)
        customView = findViewById(R.id.customView)
    }

    private fun readFileFromAssets(): Bitmap? {
        try {
            val inputStream = assets.open(fileName)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            ToastUtils.show(this, "File not exist!")
            return null
        }
    }

    private fun setData(bitmap: Bitmap) {
        imageViewSrc.setImageBitmap(bitmap)
        imageViewBg.background = BitmapDrawable(resources, bitmap)

        val holder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val canvas = holder.lockCanvas()
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                holder.unlockCanvasAndPost(canvas)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                val canvas = textureView.lockCanvas()
                canvas?.let {
                    it.drawBitmap(bitmap, 0f, 0f, null)
                    textureView.unlockCanvasAndPost(it)
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

        customView.setBitmap(bitmap)
    }
}