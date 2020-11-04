package team.hack_reva.cooklabs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LauncherActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal val TIME_OUT = 500
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
                                Handler().postDelayed(
                                        {
                                            Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                            val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                                            Log.d("DEBUG", "User is already logged in, so main activity is launched")
                                            mAuthListener?.let { firebaseAuth?.removeAuthStateListener(it) }
                                            startActivity(intent)
                                            finish()
                                        }, TIME_OUT.toLong())
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
