package team.hack_reva.cooklabs

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_fill_details.*
import kotlinx.android.synthetic.main.activity_login.*


class FillDetailsActivity : AppCompatActivity() {
    companion object {
        val image_pick_code = 1000
        val permission_code = 1001
    }

    lateinit var choosen_image_uri: Uri
    var image_choosen = false
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(255, 92, 126)
        setContentView(R.layout.activity_fill_details)
        continue_btn.setOnClickListener {
            if (name.text.isNotEmpty()) {
                val my_name = name.text.toString()
                var email = email_address.text.toString()
                if (isValidEmail(email)) {
                    if (image_choosen) {
                        val progressDialog = ProgressDialog(this)
                        progressDialog.setMessage("Uploading your profile picture")
                        progressDialog.setCancelable(false)
                        progressDialog.show()
                        uploadImageGetUrl(uri_of_the_image = choosen_image_uri)
                        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                        val phonenumber = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
                        UpdateToDataStore( uid, my_name, email, phonenumber)
                        progressDialog.dismiss()
                        val intent = Intent(this@FillDetailsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Choose your profile picture by tapping on the avatar", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Email is not valid", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_LONG).show()
            }
        }

        profile_image_btn.setOnClickListener {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("opening gallery")
            progressDialog.setCanceledOnTouchOutside(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied

                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, permission_code)
                } else {
                    //permission granted
                    progressDialog.show()
                    pickimagefromgallery()
                }
            }
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun pickimagefromgallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, image_pick_code)

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            permission_code -> {
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
        progressDialog.dismiss()
        if (resultCode == Activity.RESULT_OK && requestCode == image_pick_code) {
            profile_image_btn.setImageURI(data?.data)
            choosen_image_uri = data?.data!!
            image_choosen = true
        }
    }

    private var doubleBackToExit = false
    override fun onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExit = true
        Toast.makeText(this, "Press back button again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExit = false }, 2000)
    }

    fun uploadImageGetUrl(uri_of_the_image: Uri) {
        var url = ""
        val storage = FirebaseStorage.getInstance().reference
        val authuid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val uploadTask = storage.child("user-accounts").child("profile-pictures").child(authuid).putFile(uri_of_the_image)
        Log.d("FETCH_ERROR", "upload task given")
        uploadTask.addOnFailureListener{
            Log.d("FETCH_ERROR", it.toString())

        }.addOnSuccessListener {
            url = it.storage.downloadUrl.toString()
            Log.d("NOERROR", "the link got is ; "+ url)
        }
    }

    private fun UpdateToDataStore(uid:String, name:String, email:String, phonenumber:String){
        val hashMap = HashMap<String, String>()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["phone-number"] = phonenumber
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("user-accounts").document(uid).set(hashMap)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully written!")
                    Toast.makeText(this, "Thank you $name, you may proceed now", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error writing document", e)
                    Toast.makeText(this, "Error saving the user details. Please try again", Toast.LENGTH_SHORT).show()
                }
    }

}