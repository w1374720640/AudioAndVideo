package demo.audioandvideo.utils

import androidx.lifecycle.Lifecycle
import java.util.*

/**
 * 不断自增的计时器，每秒回调一次总时长，支持暂停、继续
 *
 * Created by wang on 2021/11/7.
 */
class TimerCounter(private val lifecycle: Lifecycle? = null, private val callback: (Long) -> Unit) {
    private var lastTime = 0L
    private var totalTime = 0L

    private val timer = Timer()
    private val timerTask = object : TimerTask() {
        override fun run() {
            lifecycle?.let {
                if (!it.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                    stop()
                    return
                }
            }
            if (lastTime != 0L) {
                val currentTime = System.currentTimeMillis()
                totalTime += currentTime - lastTime
                lastTime = currentTime
            }
            callback(totalTime)
        }
    }

    fun start() {
        lastTime = System.currentTimeMillis()
        timer.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    fun pause() {
        totalTime += System.currentTimeMillis() - lastTime
        lastTime = 0L
    }

    fun resume() {
        lastTime = System.currentTimeMillis()
    }

    fun stop() {
        timer.cancel()
    }
}