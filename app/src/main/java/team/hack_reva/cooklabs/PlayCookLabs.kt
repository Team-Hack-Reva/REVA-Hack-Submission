package team.hack_reva.cooklabs

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PlayCookLabs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_cook_labs)
        window.statusBarColor = Color.rgb(255, 92, 126)

    }
}