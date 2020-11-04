package team.hack_reva.cooklabs

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import team.hack_reva.cooklabs.ui.main.ViewPagerAdapter
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var name_of_lab:String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        window.statusBarColor = Color.rgb(249,74,100)
        tabs.addTab(tabs.newTab().setText("Recent Labs"))
        tabs.addTab(tabs.newTab().setText("My Creations"))
        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.bringToFront()
        val adapter = ViewPagerAdapter(this, supportFragmentManager, tabs.tabCount)
        view__pager.adapter = adapter
        view__pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view__pager))

        floating_btn.setOnClickListener{
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.custom_alert_dialog)
            val create_btn = dialog.findViewById(R.id.button) as Button
            val lab_name_view = dialog.findViewById(R.id.editText) as EditText
            create_btn.setOnClickListener {
                if (lab_name_view.text.isNotEmpty()){
                    name_of_lab = lab_name_view.text.toString()
                    startActivity(Intent(this@MainActivity, CreateLabActivity::class.java))
                }
            }
            dialog.show()
        }

    }
}