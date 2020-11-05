//Activity description for Login page functionality
package team.hack_reva.cooklabs

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi

class LoginActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    companion object{
        var phone_number_comobj:String = ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        window.statusBarColor = rgb(249,74,100)
        var getOtpBtn = findViewById<Button>(R.id.get_otp)
        var phone_number_view = findViewById<EditText>(R.id.phone_number)
        var country_code_view = findViewById<EditText>(R.id.country_code)
        country_code_view.isEnabled = false
        getOtpBtn.setOnClickListener {
            var country_code = country_code_view.text.toString()
            country_code = "91"  //Defaulting country code to 91 (India)
            val phone_number = phone_number_view.text.toString()
            if (country_code.isNotEmpty() and phone_number.isNotEmpty()){ //Conditional statements to check validity of phone number
                if (phone_number.length < 10) {
                    Toast.makeText(this, "Phone number cannot be less than 10", Toast.LENGTH_SHORT).show()
                }else if(phone_number.length > 10){
                    Toast.makeText(this, "Phone number cannot be greater than 10", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Looks great!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, VerifyNumbersActivity::class.java)
                    val full_number = "+$country_code$phone_number"
                    intent.putExtra("number", full_number)
                    phone_number_comobj = full_number
                    startActivity(intent)
                }
            }else{
                Toast.makeText(this, "Phone number cannot be left empty", Toast.LENGTH_SHORT).show() //Fail condition indicating phone no. empty
            }
        }
    }
}
