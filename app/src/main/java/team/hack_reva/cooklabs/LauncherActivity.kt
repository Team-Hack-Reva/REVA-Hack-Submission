package team.hack_reva.cooklabs

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LauncherActivity : AppCompatActivity() {
    companion object{
        lateinit var nameOfuserOfTheApp:String
        lateinit var emailOfuserOfTheApp:String
        lateinit var reference_path_to_play_the_lab:String
    }
    private var firebaseAuth: FirebaseAuth? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal val TIME_OUT = 4000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = user.uid.toString()
                val db = FirebaseFirestore.getInstance()
                db.collection("user-accounts")
                        .document("$uid")
                        .get()
                        .addOnSuccessListener { document ->
                            if(document.data != null){
                                nameOfuserOfTheApp = document.getString("name").toString()
                                emailOfuserOfTheApp = document.getString("email").toString()
                                Firebase.dynamicLinks
                                    .getDynamicLink(intent)
                                    .addOnSuccessListener(this) { pendingDynamicLinkData ->
                                        // Get deep link from result (may be null if no link is found)
                                        var deepLink: Uri? = null
                                        if (pendingDynamicLinkData != null) {
                                            deepLink = pendingDynamicLinkData.link
                                            Handler().postDelayed(
                                                {
                                                    Log.d("CHECKTHIS","SUCCESS CALLBACK, DYNAMIC LINK found")
                                                    val intent = Intent(this@LauncherActivity, PlayCookLabs::class.java)
                                                    Log.d("DEBUG", "User is already logged in and found dynamic link, so playcooklabsaccount is launched")
                                                    mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                                    reference_path_to_play_the_lab = deepLink.toString()
                                                    startActivity(intent)
                                                    finish()
                                                }, TIME_OUT.toLong())
                                        }else{
                                            Log.d("CHECKTHIS","SUCCESS CALLBACK, DYNAMIC LINK not found")
                                            Handler().postDelayed(
                                                {
                                                    val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                                                    Log.d("DEBUG", "User is already logged in, so main activity is launched")
                                                    mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                                    startActivity(intent)
                                                    finish()
                                                }, TIME_OUT.toLong())
                                        }

                                    }
                                    .addOnFailureListener(this) { e -> Log.w("TAG", "getDynamicLink:onFailure", e)
                                        Log.d("CHECKTHIS","FAILED CALLBACK, FAILED DYNAMICLINK")
                                        Handler().postDelayed(
                                            {
                                                val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                                                Log.d("DEBUG", "User is already logged in, so main activity is launched")
                                                mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                                startActivity(intent)
                                                finish()
                                            }, TIME_OUT.toLong())
                                    }
                            }else{
                                Handler().postDelayed(
                                        {
                                            Log.d("DEBUG", "User is logged inbut no data, so filldetails activity is launched")
                                            val intent = Intent(this@LauncherActivity, FillDetailsActivity::class.java)
                                            Log.d("DEBUG", "User is already logged in, so main activity is launched")
                                            mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                            startActivity(intent)
                                            finish()
                                        }, TIME_OUT.toLong())
                            }
                        }.addOnFailureListener {
                            FirebaseAuth.getInstance().signOut()
                            Handler().postDelayed(
                                    {
                                        FirebaseAuth.getInstance().signOut()
                                        Log.d("DEBUG", "User is not logged in, so login activity is launched")
                                        startActivity(Intent(this@LauncherActivity, LoginActivity::class.java))
                                        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                        mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                        finish()
                                    }, TIME_OUT.toLong()
                            )

                        }

            } else {
                // Wait for three seconds and move to main activity if login is successful, or else go for login/register activity
                Handler().postDelayed(
                    {
                        Log.d("DEBUG", "User is not logged in, so login activity is launched")
                        startActivity(Intent(this@LauncherActivity, LoginActivity::class.java))
                        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                        finish()
                    }, TIME_OUT.toLong()
                )
            }
        }

    }

    override fun onStart(){
        super.onStart()
        mAuthListener?.let { firebaseAuth?.addAuthStateListener(it) }
    }

    override fun onResume() {
        super.onResume()
        mAuthListener?.let { firebaseAuth?.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        mAuthListener?.let { firebaseAuth?.removeAuthStateListener (it) }
    }

}
