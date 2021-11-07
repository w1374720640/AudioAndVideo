package demo.audioandvideo.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 *
 * Created by wang on 2021/11/7.
 */
object ToastUtils {
    private var toast: Toast? = null

    fun show(context: Context, message: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(context, context.getString(message), duration)
    }

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (toast == null) {
            toast = Toast.makeText(context.applicationContext, message, duration)
        } else {
            toast?.setText(message)
            toast?.duration = duration
        }
        // 支持子线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            toast?.show()
        } else {
            Handler(Looper.getMainLooper()).post { toast?.show() }
        }
    }
}