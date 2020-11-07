package team.hack_reva.cooklabs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_lab.*
import kotlinx.android.synthetic.main.activity_fill_details.*
import team.hack_reva.cooklabs.LauncherActivity.Companion.nameOfuserOfTheApp
import team.hack_reva.cooklabs.MainActivity.Companion.name_of_lab
import java.io.File
import java.io.IOException
import java.lang.StringBuilder


class CreateLabActivity : AppCompatActivity() {
    lateinit var name_of_img:String
    private var output: String? = null
    lateinit var choosen_image_uri: Uri
    var image_choosen = false
    lateinit var progressDialog: ProgressDialog
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    var count = 1
    lateinit var waveRecorder:WaveRecorder
    lateinit var filepath:String
    lateinit var audioFilesUrilist:MutableList<Uri>
    companion object{
        lateinit var adapter:StepsRecordAdapter
        lateinit var list:MutableList<ListViewData>
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lab)
        window.statusBarColor = Color.rgb(255, 92, 126)
        list = mutableListOf<ListViewData>()
        adapter = StepsRecordAdapter(this, R.layout.item_view_createlab, list)
        list_view_steps.adapter = adapter
        create_record_btn.setOnTouchListener {v, event->
            if(event.action==MotionEvent.ACTION_DOWN){
                val dir = File(externalCacheDir?.absolutePath  + "/CookLabs/AudioFiles/temp/")
                if(!dir.exists()){
                    dir.mkdirs()
                }
                output = externalCacheDir?.absolutePath  + "/CookLabs/AudioFiles/temp/step-$count.mp3"
                //output = Environment.getExternalStorageDirectory().absolutePath + "/CookLabs/AudioFiles/temp/step-$count.mp3"
                mediaRecorder = MediaRecorder()
                mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                mediaRecorder?.setOutputFile(output)
                Toast.makeText(this, "started recording", Toast.LENGTH_SHORT).show()
                startRecording()
            }else if(event.action == MotionEvent.ACTION_UP){
                Toast.makeText(this, "Your voice is saved", Toast.LENGTH_SHORT).show()
                stopRecording()
                list.add(ListViewData("Step-", output!!))
                adapter.notifyDataSetChanged()
                count+=1
            }
            return@setOnTouchListener true
        }

        //On-Click Event listener for stop button       
        create_stop_btn.setOnClickListener { 
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Action cannot be reversed. Do you proceed?")
            builder.setMessage("You may need to create a new lab if you don't save it.")
            builder.setPositiveButton("Yes"){_,_->
                for(i in list){
                    val file = i.audiouri
                    File(file).delete()
                }
                finish()
            }
            builder.setNegativeButton("No"){_,_->

            }
            val alertDialog = builder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
        //One-Click Event listener for save button
        create_save_btn.setOnClickListener {
            if(list.isNotEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Wish to add a cover photo for this cook lab?")
                builder.setMessage("Default image will be used if image is not provided.")
                builder.setPositiveButton("Upload image & save"){_,_->
                    progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("opening gallery")
                    progressDialog.setCanceledOnTouchOutside(false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED
                        ) {
                            //permission denied

                            val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermissions(permissions, FillDetailsActivity.permission_code)
                        } else {
                            //permission granted
                            progressDialog.show()
                            pickimagefromgallery()
                        }
                    }
                }
                builder.setNegativeButton("Just save"){_,_->
                    progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Started to upload all your audio")
                    name_of_img = generateRandomName()
                    progressDialog.show()
                    BatchUploadAudioFiles()
                }
                builder.setNeutralButton("cancel"){_,_->

                }
                val alertDialog = builder.create()
                alertDialog.setCancelable(true)
                alertDialog.show()

            }else{
                Toast.makeText(this,"Empty Lab sessions cannot be created. please create a single step", Toast.LENGTH_SHORT).show()
            }
        }
        
    }
    //Function for the Start Recording functionality
    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show() //Prompt to denote successful recording start.
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    //Function for the Stop Recording functionality
    private fun stopRecording(){
        try {
            if(state){
                mediaRecorder?.stop()
                mediaRecorder?.release()
                state = false
            }else{
                Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show() //Prompt to denote successful recording stop.
            }
        } catch (stopException: RuntimeException) {
            // handle cleanup here
        }

    }
    //Function to open image from gallery for usage
    private fun pickimagefromgallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, FillDetailsActivity.image_pick_code)

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            FillDetailsActivity.permission_code -> {
                if (grantResults.size > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
                ) {
                    progressDialog.show()
                    pickimagefromgallery()
                } else {
                    Toast.makeText(
                            this,
                            "Permission denied!",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == FillDetailsActivity.image_pick_code) {
            choosen_image_uri = data?.data!!
            image_choosen = true
            uploadImageGetUrl(choosen_image_uri)

        }else{
            Toast.makeText(this, "Failed to select the image, please try again",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    fun generateRandomName(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var passWord = ""
        for (i in 0..31) {
            passWord += chars[Math.floor(Math.random() * chars.length).toInt()]
        }
        return passWord
    }

    fun uploadImageGetUrl(uri_of_the_image: Uri) {
        var url = ""
        progressDialog.setMessage("Uploading and setting the cover picture for the cooklab")
        val storage = FirebaseStorage.getInstance().reference
        val authuid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        name_of_img = generateRandomName().toString()
        val uploadTask = storage.child("cook-labs").child(authuid).child("pictures").child(name_of_img).putFile(uri_of_the_image)
        Log.d("FETCH_ERROR", "upload task given")
        uploadTask.addOnFailureListener{
            Log.d("FETCH_ERROR", it.toString())
            progressDialog.dismiss()
            Toast.makeText(this,"Failed to upload Image of the cook lab. please try again.", Toast.LENGTH_SHORT).show()

        }.addOnSuccessListener {
            url = it.storage.downloadUrl.toString()
            Log.d("NOERROR", "the link got is ; "+ url)
            progressDialog.setMessage("Uploaded the cover picture successfully.")
            BatchUploadAudioFiles()

        }
    }
    private fun BatchUploadAudioFiles(){
        val authuid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val audio_storage = FirebaseStorage.getInstance().reference
        var current_count = 1
        val total_list = list.size
        for (i in list){
            val uri_of_audio = Uri.fromFile(File(i.audiouri))
            val step_name = "step-$current_count"
            progressDialog.setMessage("Uploading ${count-1} steps out of $total_list steps done")
            val task = audio_storage.child("cook-labs").child(authuid).child("audio").child(name_of_img).child(step_name).putFile(uri_of_audio)
            //val task = audio_storage.child("cook").putFile(uri_of_audio)
            task.addOnSuccessListener {
                Toast.makeText(this, "uploaded $current_count files", Toast.LENGTH_SHORT)
            }.addOnFailureListener {
                        Toast.makeText(this, "Failed to upload ${total_list - current_count} files. please try again.", Toast.LENGTH_SHORT).show()
                    }
            Log.d("CHECK", "EXECUTING step $current_count")
            current_count+=1
        }
        progressDialog.setMessage("Hurray, almost done. just hang a second while we finish the process.")
        val database = FirebaseFirestore.getInstance().collection("user-cook-labs").document(authuid)
                .collection("posts").document(name_of_lab)
        val hashMap = HashMap<String,Any>()
        hashMap["author"] = nameOfuserOfTheApp
        hashMap["cover-picture-path"] = "cook-labs/$authuid/pictures/$name_of_img.jpg"
        hashMap["name-of-post"] = name_of_lab
        hashMap["no-of-steps"] = current_count - 1
        hashMap["uid-of-author"] = authuid
        hashMap["audio-storage-name"] = "cook-labs/$authuid/audio/$name_of_img"
        database.set(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this,"Successfully created the lab", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Failed to create the lab", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
    }

}

