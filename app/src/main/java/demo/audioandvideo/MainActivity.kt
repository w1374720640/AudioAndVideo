package demo.audioandvideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import demo.audioandvideo.task1.DrawImageActivity
import demo.audioandvideo.task2.AudioActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.task1).setOnClickListener(this)
        findViewById<Button>(R.id.task2).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.task1 -> startActivity(Intent(this, DrawImageActivity::class.java))
            R.id.task2 -> startActivity(Intent(this, AudioActivity::class.java))
        }
    }
}