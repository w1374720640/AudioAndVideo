package demo.audioandvideo.task2

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.audioandvideo.R
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
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
    private lateinit var stopBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AudioListAdapter
    private val audioList = arrayListOf<File>()
    private lateinit var audioRecord: AudioRecord
    private var minBufferSize = 0
    private lateinit var buffer: ByteArray
    private var recordState = RecordState.STOP

    enum class RecordState {
        STOP, RECORDING, PAUSE
    }

    companion object {
        const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
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
        stopBtn = findViewById(R.id.stopBtn)
        initRecyclerView()

        startBtn.setOnClickListener(this)
        pauseBtn.setOnClickListener(this)
        stopBtn.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        getAudioList()
        adapter = AudioListAdapter(this, audioList)
        recyclerView.adapter = adapter
    }

    private fun getAudioList() {
        audioList.clear()
        parentFile.listFiles()?.let {
            audioList.addAll(it)
        }
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowPermission ->
            if (allowPermission) {
                if (recordState == RecordState.PAUSE) {
                    recordState = RecordState.RECORDING
                    startBtn.isEnabled = false
                    pauseBtn.isEnabled = true
                    stopBtn.isEnabled = true
                } else {
                    initAudioRecord()
                    audioRecord.startRecording()
                    recordState = RecordState.RECORDING
                    startBtn.isEnabled = false
                    pauseBtn.isEnabled = true
                    stopBtn.isEnabled = true
                    lifecycleScope.launch {
                        val result = readRecordingData()
                        showResult(result)
                    }
                }
            } else {
                showPermissionRejectDialog()
            }
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.startBtn -> {
                permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
            }
            R.id.pauseBtn -> {
                recordState = RecordState.PAUSE
                startBtn.isEnabled = true
                pauseBtn.isEnabled = false
                stopBtn.isEnabled = true
            }
            R.id.stopBtn -> {
                recordState = RecordState.STOP
                startBtn.isEnabled = true
                pauseBtn.isEnabled = false
                stopBtn.isEnabled = false
            }
        }
    }

    private fun showPermissionRejectDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_reject_title)
            .setMessage(R.string.reject_record_audio)
            .setPositiveButton(R.string.dialog_positive_btn) { _, _->

            }
            .show()
    }

    private fun initAudioRecord() {
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        Log.d("initAudioRecord", "minBufferSize=$minBufferSize")
        audioRecord = AudioRecord(
            AUDIO_SOURCE,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
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
                    val read = audioRecord.read(buffer, 0, minBufferSize)
                    if (recordState == RecordState.RECORDING && read > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                }
                outputStream.close()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun showResult(result: Result<Unit>) {
        withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                Toast.makeText(
                    this@AudioActivity,
                    R.string.file_write_succeed,
                    Toast.LENGTH_SHORT
                ).show()
                getAudioList()
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(
                    this@AudioActivity,
                    R.string.file_write_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}