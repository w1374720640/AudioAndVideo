package demo.audioandvideo.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import demo.audioandvideo.R

/**
 *
 * Created by wang on 2021/11/7.
 */
object DialogUtils {
    /**
     * 只有一个确定按钮，点击无响应事件
     */
    fun showDialog(context: Context, titleId: Int, contentId: Int) {
        showDialog(
            context,
            context.getString(titleId),
            context.getString(contentId)
        )
    }

    /**
     * 只有一个确定按钮，点击无响应事件
     */
    fun showDialog(context: Context, title: String, content: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton(R.string.dialog_positive_btn, null)
            .show()
    }

    /**
     * 根据是否有响应事件，判断是否需要显示确认按钮或取消按钮
     */
    fun showDialog(
        context: Context, titleId: Int, contentId: Int,
        positiveClickListener: DialogInterface.OnClickListener?,
        negativeClickListener: DialogInterface.OnClickListener?
    ) {
        showDialog(
            context,
            context.getString(titleId),
            context.getString(contentId),
            positiveClickListener,
            negativeClickListener
        )
    }

    /**
     * 根据是否有响应事件，判断是否需要显示确认按钮或取消按钮
     */
    fun showDialog(
        context: Context, title: String, content: String,
        positiveClickListener: DialogInterface.OnClickListener?,
        negativeClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(content)
        if (positiveClickListener != null) {
            builder.setPositiveButton(R.string.dialog_positive_btn, positiveClickListener)
        }
        if (negativeClickListener != null) {
            builder.setNegativeButton(R.string.dialog_negative_btn, negativeClickListener)
        }
        builder.show()
    }
}