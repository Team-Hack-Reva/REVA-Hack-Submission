package team.hack_reva.cooklabs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapterMainActivity(private var postsList:List<Posts>):
RecyclerView.Adapter<PostAdapterMainActivity.MyViewHolder>(){
    lateinit var context:Context
    inner class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        var title:TextView = view.findViewById(R.id.item_heading)
        var backgroundImage = view.findViewById<ImageView>(R.id.background_image)
        var profileimage = view.findViewById<CircleImageView>(R.id.propic_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cooklabs_main_activity, parent, false)
        context = parent.context

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = postsList[position]
        holder.title.text = post.heading_text
        val d = post.background_pic_url
        Log.d("CHECK", "ADAPTER $d")

        if(post.user_pic_url!=""){
            Picasso.get().load(post.user_pic_url).placeholder(R.drawable.ic_baseline_account_circle_24).into(holder.profileimage)
        }else{
            holder.profileimage.setImageResource(R.drawable.ic_baseline_account_circle_24)
        }
        if(post.background_pic_url!=""){
            Picasso.get().load(post.background_pic_url).placeholder(R.drawable.picsart_11_05_07_51_54).into(holder.backgroundImage)
        }else{
            holder.backgroundImage.setImageResource(R.drawable.picsart_11_05_07_51_54)
        }


    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}

data class Posts(var user_pic_url:String, var background_pic_url:String, var heading_text:String, var reference:String)