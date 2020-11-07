package team.hack_reva.cooklabs

import android.app.ProgressDialog
import android.media.MediaPlayer
import android.media.tv.TvContract
import android.net.Uri
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_play_cook_labs.*
import org.json.JSONObject
import team.hack_reva.cooklabs.LauncherActivity.Companion.reference_path_to_play_the_lab
import java.io.File

class PlayCookLabs : AppCompatActivity() {
    var count = 0
    companion object{
         var companion_GET_DOUBLE:Double = 0.0
    }
    var playing_file = false
    lateinit var current_url: String
    lateinit var mediaPlayer: MediaPlayer
    var url_list = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_cook_labs)
        val progressdialog = ProgressDialog(this)
        progressdialog.setMessage("please wait while we prepare your lab.")
        progressdialog.show()
        window.statusBarColor = Color.rgb(255, 92, 126)
        val response = reference_path_to_play_the_lab.substring(37)
        // 0 - heading, 1 - reference, 2 - uid of author, 3 - author, 4 - user pic url
        val array = (response.substring(1, response.length - 1)).split(",") // Array is obtained
        val title = array[0].substring(1)
        val reference = array[1].substring(1)
        val uid_of_author = array[2].substring(1)
        val author = array[3].substring(1)
        var user_pic_url = array[4].substring(1)
        Log.d("VALUECHECK", "The title is"+title)
        Log.d("VALUECHECK", "The reference is"+reference)
        Log.d("VALUECHECK", "The uid of author is" + uid_of_author)
        Log.d("VALUECHECK", "The author is"+author)
        Log.d("VALUECHECK", "the user url is"+user_pic_url)

        Picasso.get().load(user_pic_url).placeholder(R.drawable.applogo).into(author_pic)
        val ref = reference.split("/")
        val db = FirebaseFirestore.getInstance().collection(ref[0]).document(ref[1])
            .collection(ref[2]).document(ref[3])
        db.get().addOnSuccessListener {
            if (it.data!=null){
                val count = it.getDouble("no-of-steps")?.toInt()
                Log.d("CHEK", "THEC COUNT IS $count")
                val audiopath = it.getString("audio-storage-name")
                val json = JSONObject()
                json.put("author", it.getString("author").toString())
                json.put("cover-picture-path", it.getString("cover-picture-path").toString())
                json.put("name-of-post",  it.getString("name-of-post").toString())
                json.put("no-of-steps",  it.getDouble("no-of-steps"))
                companion_GET_DOUBLE = it.getDouble("no-of-steps")?.toDouble()!!
                json.put("uid-of-author",  it.getString("uid-of-author").toString())
                json.put("audio-storage-name",  it.getString("audio-storage-name").toString())
                BatchUploadAudioFiles(json)
                url_list.clear()
                for (i in 1..count!!){
                    val path = "$audiopath/step-$i"
                    Log.d("TAG", "THE PATH IS:$audiopath/step-$i")
                    val audio_storage = FirebaseStorage.getInstance().reference.child(path).downloadUrl
                            .addOnSuccessListener {
                                url_list.add(it.toString())
                                val ss = it.toString()
                                Log.d("TAG", "The link of the url file is :"+ss)
                            }.addOnFailureListener{
                                Log.d("TAG", "Failing to download the link")
                            }
                }
                progressdialog.dismiss()
            }
        }.addOnFailureListener {
            Log.e("TAG","FAILED TO FETCH THE RESOURCE ${it.printStackTrace()}")
        }
        mediaPlayer = MediaPlayer()
        play_pause_step.setOnClickListener {
            if(url_list.isNotEmpty()){
                if (playing_file) {
                    if (mediaPlayer.isPlaying) {
                        show_step_count.text = "Step ${count+1}/${url_list.size}"
                        mediaPlayer.stop()
                        playing_file = false
                        play_pause_step.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                } else {
                    if (!mediaPlayer.isPlaying) {
                        show_step_count.text = "Step ${count+1}/${url_list.size}"
                        current_url = url_list.get(count)
                        mediaPlayer = MediaPlayer.create(this, Uri.parse(current_url))
                        mediaPlayer.start()
                        mediaPlayer.setOnCompletionListener {
                            playing_file = false
                            play_pause_step.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        }
                    }
                    play_pause_step.setImageResource(R.drawable.ic_baseline_stop_24)
                    playing_file = true
                }
            }else{
                Toast.makeText(this,"Please hang on a second as we load all the audio instructions", Toast.LENGTH_SHORT).show()
            }

        }
        previous_step.setOnClickListener {
            if(count==0){
                Toast.makeText(this,"This is your first step.", Toast.LENGTH_SHORT).show()
            }else{
                count = count - 1
                show_step_count.text = "Step ${count+1}/${url_list.size}"
                if(mediaPlayer.isPlaying){
                    mediaPlayer.stop()
                    current_url = url_list.get(count)
                    mediaPlayer = MediaPlayer.create(this, Uri.parse(current_url))
                    mediaPlayer.start()
                    play_pause_step.setImageResource(R.drawable.ic_baseline_stop_24)
                    playing_file = true
                    mediaPlayer.setOnCompletionListener {
                        playing_file = false
                        play_pause_step.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }


            }
        }
        next_step.setOnClickListener {
            if(count==(url_list.size-1)){
                Toast.makeText(this, "This is your last step.", Toast.LENGTH_SHORT).show()
            }else{
                count = count + 1
                show_step_count.text = "Step ${count+1}/${url_list.size}"
                if(mediaPlayer.isPlaying){
                    mediaPlayer.stop()
                    current_url = url_list.get(count)
                    mediaPlayer = MediaPlayer.create(this, Uri.parse(current_url))
                    mediaPlayer.start()
                    play_pause_step.setImageResource(R.drawable.ic_baseline_stop_24)
                    playing_file = true
                    mediaPlayer.setOnCompletionListener {
                        playing_file = false
                        play_pause_step.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    private fun BatchUploadAudioFiles(json:JSONObject){
        val authuid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val database = FirebaseFirestore.getInstance().collection("user-cook-labs").document(authuid)
                .collection("recent-posts").document(json.get("name-of-post").toString())
        val hashMap = HashMap<String,Any>()
        hashMap["author"] = json.get("author").toString()
        hashMap["cover-picture-path"] = json.get("cover-picture-path").toString()
        hashMap["name-of-post"] = json.get("name-of-post").toString()
        hashMap["no-of-steps"] = companion_GET_DOUBLE
        Log.d("CHECKTHISSSS","${hashMap["no-of-steps"]}")

        hashMap["uid-of-author"] = json.get("uid-of-author").toString()
        hashMap["audio-storage-name"] = json.get("audio-storage-name").toString()
        database.set(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this,"This lab is added to your recent successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Failed to add the lab to recent", Toast.LENGTH_SHORT).show()
                }
    }

}
