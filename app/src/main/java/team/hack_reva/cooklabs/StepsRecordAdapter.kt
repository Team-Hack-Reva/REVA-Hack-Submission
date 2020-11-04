package team.hack_reva.cooklabs

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.database.DataSetObserver
import android.graphics.ColorSpace
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import team.hack_reva.cooklabs.CreateLabActivity.Companion.adapter
import team.hack_reva.cooklabs.CreateLabActivity.Companion.list
import java.io.File

class StepsRecordAdapter(context:Context, var resource:Int, var items:List<ListViewData>):
    ArrayAdapter<ListViewData>(context, resource, items){
    var mediaPlayer = MediaPlayer()
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(resource, null)
        val button_playpause = view.findViewById<FloatingActionButton>(R.id.play_pause_btn)
        val button_delete = view.findViewById<FloatingActionButton>(R.id.delete_btn)
        val step_count_view = view.findViewById<TextView>(R.id.step_count)
        var step:ListViewData = items[position]
        val stepname = step.stepname
        var play_flag = false
        step_count_view.text = stepname+(position+1).toString()

        button_playpause.setOnClickListener {
            if(play_flag){
                mediaPlayer.stop()
                button_playpause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                play_flag = false
            }else{
                play_flag = true
                button_playpause.setImageResource(R.drawable.ic_baseline_stop_24)
                val uri = Uri.fromFile(File(step.audiouri))
                mediaPlayer = MediaPlayer.create(context, uri)
                if(!mediaPlayer.isPlaying){
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener {
                        play_flag = false
                        button_playpause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                    }

                }
            }


        }

        button_delete.setOnClickListener {
            //items.drop(position)
            CreateLabActivity.list.removeAt(position)
            CreateLabActivity.adapter.notifyDataSetChanged()
        }
        return view
    }
}


data class ListViewData(var stepname: String, var audiouri:String)