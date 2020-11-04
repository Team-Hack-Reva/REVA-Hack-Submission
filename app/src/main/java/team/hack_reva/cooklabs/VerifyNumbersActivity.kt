package team.hack_reva.cooklabs

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import team.hack_reva.cooklabs.LoginActivity.Companion.phone_number_comobj
import java.util.*
import java.util.concurrent.TimeUnit


class VerifyNumbersActivity : AppCompatActivity() {
    lateinit var storedVerificationId:String
    lateinit var resendToken:String
    //These are the objects needed
    //It is the verification id that will be sent to the user
    private val mVerificationId: String? = null
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mAuth: FirebaseAuth? = null
    private lateinit var timerObj:CountDownTimer
    lateinit var progress_bar:ProgressBar
    lateinit var provide_otp_et:EditText
    lateinit var signin_btn:Button
    lateinit var resend_code:Button
    lateinit var timer:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_numbers)
        mAuth = FirebaseAuth.getInstance()
        progress_bar = findViewById<ProgressBar>(R.id.progressBar)
        provide_otp_et = findViewById<EditText>(R.id.provide_otp)
        signin_btn = findViewById<Button>(R.id.signin)
        resend_code = findViewById<Button>(R.id.resend_code)
        timer = findViewById(R.id.timer_text)
        val intent = intent
        val mobile = intent.getStringExtra("number")
        timerObj = object : CountDownTimer(60000,1000){
            override fun onFinish() {
                resend_code.isEnabled = true
                timer.visibility = View.INVISIBLE
            }

            override fun onTick(p0: Long) {
                val sec = p0/1000
                timer.text = "Wait for $sec seconds to resend OTP"
                timer.visibility = View.VISIBLE
            }

        }
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Log.d("auth", "on verifiation completed: $p0")
                provide_otp_et.setText(p0.smsCode.toString())
                progress_bar.visibility = View.INVISIBLE
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                progress_bar.visibility = View.INVISIBLE
                Log.d("auth", "on verification failed ",p0)
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@VerifyNumbersActivity, "Invalid credentials, please try again", Toast.LENGTH_SHORT).show()
                } else if (p0 is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@VerifyNumbersActivity, "Error with server as sms quota reached", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("auth", "onCodeSent:$verificationId")
                Toast.makeText(this@VerifyNumbersActivity, "OTP is sent to your phone.", Toast.LENGTH_SHORT).show()
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token.toString()
                signin_btn.isEnabled = true
                progress_bar.visibility = View.INVISIBLE
                timerObj.start()
            }
        }



        sendVerificationCode(phone_number_comobj)
        resend_code.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            resend_code.isEnabled = false
            signin_btn.isEnabled = false
            sendVerificationCode(phone_number_comobj)
        }

        signin_btn.setOnClickListener{
            if(provide_otp_et.text.isNotEmpty()){
                val code = provide_otp_et.text.toString()
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)

            }else{
                Toast.makeText(this@VerifyNumbersActivity, "Wait for the OTP, and then provide the same.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Hi user, please wait while we get you signed in.")
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    val user = task.result?.user?.phoneNumber.toString()
                    Toast.makeText(this@VerifyNumbersActivity,"You are logged in successfully", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@VerifyNumbersActivity, LauncherActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    // Sign in failed, display a message and update the UI
                    Toast.makeText(this@VerifyNumbersActivity,"Failed, try again in sometime.", Toast.LENGTH_SHORT).show()
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    timerObj.onFinish()
                    signin_btn.isEnabled = true
                    resend_code.isEnabled = true
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this@VerifyNumbersActivity,"Invalid OTP, try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun sendVerificationCode(mobile: String) {
        val options = mAuth?.let {
            PhoneAuthOptions.newBuilder(it)
                .setPhoneNumber(mobile)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        }
        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

}