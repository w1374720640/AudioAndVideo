package demo.audioandvideo.task2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.audioandvideo.R
import java.io.File

/**
 *
 * Created by wang on 2021/10/31.
 */
class AudioListAdapter(private var context: Context, private var datas: List<File>)
    : RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_audio_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameView = itemView.findViewById<TextView>(R.id.nameView)
        val progressBar  = itemView.findViewById<ProgressBar>(R.id.progressBar)
        val timeView = itemView.findViewById<TextView>(R.id.timeTv)
        val dateView = itemView.findViewById<TextView>(R.id.dateTv)

        fun bind(data: File) {
            nameView.text = data.name
        }
    }
}