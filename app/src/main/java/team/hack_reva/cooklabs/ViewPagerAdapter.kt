package team.hack_reva.cooklabs.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import team.hack_reva.cooklabs.My_Creations_Fragment
import team.hack_reva.cooklabs.My_recent_Fragment

internal class ViewPagerAdapter(var c:Context, manager: FragmentManager?,var totalTabs:Int) : FragmentPagerAdapter(manager!!) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> My_recent_Fragment()
            1-> My_Creations_Fragment()
            else -> My_recent_Fragment()
        }

    }

    override fun getCount(): Int {
        return totalTabs
    }

}