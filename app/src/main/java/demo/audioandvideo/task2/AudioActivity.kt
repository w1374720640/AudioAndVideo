package demo.audioandvideo.task2

import android.Manifest
import android.annotation.SuppressLint
import android.media.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.audioandvideo.R
import demo.audioandvideo.utils.DialogUtils
import demo.audioandvideo.utils.LogUtils
import demo.audioandvideo.utils.TimerCounter
import demo.audioandvideo.utils.ToastUtils
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by wang on 2021/10/23.
 */
class AudioActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var parentFile: File
    private lateinit var timeTv: TextView
    private lateinit var startBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var resumeBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AudioListAdapter
    private val audioList = arrayListOf<AudioListData>()

    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    private lateinit var buffer: ByteArray
    private var recordState = RecordState.STOP
    private var timerCounter: TimerCounter? = null

    private var audioTrack: AudioTrack? = null
    private var trackBufferSize = 0
    private lateinit var trackBuffer: ByteArray

    private var audioPlayPosition = -1
    private var trackState = TrackState.STOP


    enum class RecordState {
        STOP, RECORDING, PAUSE
    }

    enum class TrackState {
        STOP, RUNNING, PAUSE
    }

    companion object {
        const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        const val SAMPLE_RATE = 44100
        const val CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val STREAM_TYPE = AudioManager.STREAM_MUSIC
        const val CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        initParentFile()
        initView()
    }

    private fun initParentFile() {
        var file = getExternalFilesDir(null);
        if (file == null) {
            file = filesDir
        }
        parentFile = File(file, "Audio")
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
    }

    private fun initView() {
        timeTv = findViewById(R.id.timeTv)
        startBtn = findViewById(R.id.startBtn)
        pauseBtn = findViewById(R.id.pauseBtn)
        resumeBtn = findViewById(R.id.resumeBtn)
        stopBtn = findViewById(R.id.stopBtn)
        initRecyclerView()

        startBtn.setOnClickListener(this)
        pauseBtn.setOnClickListener(this)
        resumeBtn.setOnClickListener(this)
        stopBtn.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        getAudioList()
        adapter = AudioListAdapter(this, audioList)
        recyclerView.adapter = adapter
        adapter.itemClickListener = {
            val data = audioList[it]
            if (it == audioPlayPosition) {
                when (trackState) {
                    TrackState.STOP -> {
                        initAudioTrack()
                        audioTrack?.play()
                        trackState = TrackState.RUNNING
                        lifecycleScope.launch {
                            writeAudioTrackData(data.file)
                        }
                    }
                    TrackState.PAUSE -> {
                        audioTrack?.play()
                        trackState = TrackState.RUNNING
                    }
                    TrackState.RUNNING -> {
                        audioTrack?.pause()
                        trackState = TrackState.PAUSE
                    }
                }
            } else {
                audioPlayPosition = it
                initAudioTrack()
                audioTrack?.play()
                trackState = TrackState.RUNNING
                lifecycleScope.launch {
                    writeAudioTrackData(data.file)
                }
            }
            data.state = trackState
            adapter.notifyItemChanged(audioPlayPosition)
        }
    }

    private fun initTimerCounter() {
        timerCounter = TimerCounter(lifecycle) { time ->
            runOnUiThread {
//                LogUtils.d("time=$time")
                val hour = time / (60 * 60 * 1000)
                val minute = (time % (60 * 60 * 1000)) / (60 * 1000)
                val second = (time % (60 * 1000)) / 1000
                timeTv.text = if (hour == 0L) {
                    // 格式化字符串，不足两位前面补0
                    String.format("%02d:%02d", minute, second)
                } else {
                    String.format("%d:%02d:%02d", hour, minute, second)
                }
            }
        }
    }

    private fun getAudioList() {
        audioList.clear()
        parentFile.listFiles()?.forEach {
            audioList.add(AudioListData(it))
        }
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowPermission ->
            if (allowPermission) {
                initAudioRecord()
                audioRecord?.startRecording()
                initTimerCounter()
                timerCounter?.start()
                recordState = RecordState.RECORDING
                startBtn.isEnabled = false
                pauseBtn.isEnabled = true
                resumeBtn.isEnabled = false
                stopBtn.isEnabled = true
                lifecycleScope.launch {
                    val result = readRecordingData()
                    showResult(result)
                }
            } else {
                DialogUtils.showDialog(
                    this,
                    R.string.permission_reject_title,
                    R.string.reject_record_audio
                )
            }
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.startBtn -> {
                permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
            }
            R.id.pauseBtn -> {
                recordState = RecordState.PAUSE
                timerCounter?.pause()
                startBtn.isEnabled = false
                pauseBtn.isEnabled = false
                resumeBtn.isEnabled = true
                stopBtn.isEnabled = true
            }
            R.id.resumeBtn -> {
                recordState = RecordState.RECORDING
                timerCounter?.resume()
                startBtn.isEnabled = false
                pauseBtn.isEnabled = true
                resumeBtn.isEnabled = false
                stopBtn.isEnabled = true
            }
            R.id.stopBtn -> {
                recordState = RecordState.STOP
                timerCounter?.stop()
                startBtn.isEnabled = true
                pauseBtn.isEnabled = false
                resumeBtn.isEnabled = false
                stopBtn.isEnabled = false
            }
        }
    }

    override fun onBackPressed() {
        if (recordState != RecordState.STOP) {
            DialogUtils.showDialog(this,
                R.string.dialog_common_title,
                R.string.record_audio_back_press,
                positiveClickListener = { _, _ ->
                    recordState = RecordState.STOP
                    timerCounter?.stop()
                    finish()
                },
                negativeClickListener = { _, _ ->

                })
        } else {
            if (trackState != TrackState.STOP) {
                audioTrack?.pause()
                audioTrack?.flush()
                audioTrack?.release()
                audioTrack = null
                trackState = TrackState.STOP
                audioPlayPosition = -1
            }
            super.onBackPressed()
        }
    }

    private fun initAudioRecord() {
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT)
        LogUtils.d("minBufferSize=$minBufferSize")
        audioRecord = AudioRecord(
            AUDIO_SOURCE,
            SAMPLE_RATE,
            CHANNEL_IN_CONFIG,
            AUDIO_FORMAT,
            minBufferSize * 2
        )
        buffer = ByteArray(minBufferSize)
    }

    private suspend fun readRecordingData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val currentTime = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA)
                    .format(Date(System.currentTimeMillis()))
                val fileName = "${currentTime}.pcm"
                val file = File(parentFile, fileName)
                val outputStream = BufferedOutputStream(FileOutputStream(file))
                while (recordState != RecordState.STOP) {
                    // 使用非阻塞的方式获取数据，每次只获取一小段数据
                    val read =
                        audioRecord?.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
                            ?: -1
                    if (recordState == RecordState.RECORDING && read > 0) {
//                        LogUtils.d("read $read bytes")
                        outputStream.write(buffer, 0, read)
                    }
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        recordState = RecordState.STOP
                    }
                }
                outputStream.close()
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun showResult(result: Result<Unit>) {
        withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                ToastUtils.show(this@AudioActivity, R.string.file_write_succeed)
                getAudioList()
                adapter.notifyDataSetChanged()
            } else {
                ToastUtils.show(this@AudioActivity, R.string.file_write_failed)
            }
        }
    }

    private fun initAudioTrack() {
        trackBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_CONFIG, AUDIO_FORMAT)
        trackBuffer = ByteArray(trackBufferSize)
        audioTrack = AudioTrack(
            STREAM_TYPE,
            SAMPLE_RATE,
            CHANNEL_OUT_CONFIG,
            AUDIO_FORMAT,
            trackBufferSize * 2,
            AudioTrack.MODE_STREAM
        )
    }

    private suspend fun writeAudioTrackData(file: File) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val inputStream = BufferedInputStream(FileInputStream(file))
                while (trackState != TrackState.STOP && inputStream.available() > 0) {
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        break
                    }
                    val count = inputStream.read(trackBuffer)
                    LogUtils.d("count=$count")
                    if (count > 0) {
                        // FIXME 暂停时会直接退出循环
                        audioTrack?.write(trackBuffer, 0, count, AudioTrack.WRITE_BLOCKING)
                    }
                }
                inputStream.close()
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
                trackState = TrackState.STOP
                if (audioPlayPosition in audioList.indices) {
                    withContext(Dispatchers.Main) {
                        audioList[audioPlayPosition].state = TrackState.STOP
                        adapter.notifyItemChanged(audioPlayPosition)
                        audioPlayPosition = -1
                    }
                }
            }
        }
    }
}