package demo.audioandvideo.task2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.audioandvideo.R
import java.io.File

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

    override fun onClick(v: View) {
        when(v.id) {
            R.id.startBtn -> {

            }
            R.id.pauseBtn -> {

            }
            R.id.stopBtn -> {

            }
        }
    }
}