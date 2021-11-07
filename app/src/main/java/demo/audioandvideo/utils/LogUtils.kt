package demo.audioandvideo.utils

import android.util.Log

/**
 *
 * Created by wang on 2021/11/7.
 */
object LogUtils {
    private const val showLog = true

    fun d(message: String) {
        if (!showLog) return
        Log.d(getLogTag(), message)
    }

    fun e(message: String) {
        if (!showLog) return
        Log.e(getLogTag(), message)
    }

    /**
     * 根据方法调用栈打印出调用方的类名、方法、行数等信息
     */
    private fun getLogTag(): String {
        val array = Thread.currentThread().stackTrace
        val element = array[4]
        var className = element.className
        val index = className.lastIndexOf('.')
        if (index != -1) {
            className = className.substring(index + 1)
        }
        val methodName = element.methodName
        val lineNum = element.lineNumber
        return "${className}.${methodName}():$lineNum"
    }
}