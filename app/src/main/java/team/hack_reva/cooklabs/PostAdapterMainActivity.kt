package team.hack_reva.cooklabs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        Glide.with(context).load(post.user_pic_url).into(holder.profileimage)
        Glide.with(context).load(post.background_pic_url).into(holder.backgroundImage)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}

data class Posts(var user_pic_url:String, var background_pic_url:String, var heading_text:String, var reference:String)