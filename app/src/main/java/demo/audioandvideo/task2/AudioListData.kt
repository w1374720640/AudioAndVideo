package demo.audioandvideo.task2

import java.io.File

/**
 *
 * Created by wang on 2021/11/13.
 */
data class AudioListData(val file: File) {
    var totalTime = 0
    var createTime = 0L
    var state = AudioActivity.TrackState.STOP
}