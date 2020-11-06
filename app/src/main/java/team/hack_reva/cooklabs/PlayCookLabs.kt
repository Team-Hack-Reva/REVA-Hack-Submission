package team.hack_reva.cooklabs

import android.app.ProgressDialog
import android.media.MediaPlayer
import android.media.tv.TvContract
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_play_cook_labs.*
import team.hack_reva.cooklabs.LauncherActivity.Companion.reference_path_to_play_the_lab

class PlayCookLabs : AppCompatActivity() {
    var playing_file = false
    lateinit var current_url:String
    lateinit var mediaPlayer:MediaPlayer
    lateinit var url_list:MutableList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_cook_labs)
        val response = reference_path_to_play_the_lab.substring(37)
        // 0 - heading, 1 - reference, 2 - uid of author, 3 - author, 4 - user pic url
        val array = (response.substring(1,response.length-1)).split(",") // Array is obtained
        val title = array[0]
        val reference = array[1]
        val uid_of_author = array[2]
        val author = array[3]
        var user_pic_url = array[4]
        val photo_storage = FirebaseStorage.getInstance().reference
        Log.d("CHECKTHIS", user_pic_url)
        author_pic.setImageResource(R.drawable.applogo)
        Picasso.get().load(user_pic_url).placeholder(R.drawable.applogo).into(author_pic)
        // Download all the urls of the steps
        val progressdialog = ProgressDialog(this)
        progressdialog.setMessage("please wait while we prepare your lab.")
        progressdialog.show()
        Log.d("CHECKTHIS", "thiss is the path dude : $reference")
        val firestore = FirebaseFirestore.getInstance().collection("user-cook-labs").document(uid_of_author)
            .collection("posts").document(title).addSnapshotListener { value, error ->
                if (value != null) {
                    if(value.exists()){
                        url_list.clear()
                        val count_to_iterate = value.getDouble("no-of-steps")?.toDouble()?.toInt()
                        for (i in 1..count_to_iterate!!){
                            val ref = reference+"step-$i"
                            Log.d("CHECKING", "the $ref is the referring storage")
                            val firebaseStorage = FirebaseStorage.getInstance().reference.child(ref)
                                .downloadUrl.addOnCompleteListener {
                                    url_list.add(it.result.toString())
                                    val dl_link = it.result.toString()
                                    Log.d("CHECKING","Got the download LInk as $dl_link")
                                }.addOnFailureListener{
                                    Log.d("CHECKING","FAILEDDDDDDD")
                                }
                        }
                        progressdialog.dismiss()
                    }

                }
            }





        mediaPlayer = MediaPlayer()
        play_pause_step.setOnClickListener {
            if(playing_file){
                if(mediaPlayer.isPlaying){
                    mediaPlayer.stop()
                    playing_file = false
                    play_pause_step.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }else {
                if(!mediaPlayer.isPlaying){
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
        }

        previous_step.setOnClickListener {

        }

        next_step.setOnClickListener {

        }


    }
}