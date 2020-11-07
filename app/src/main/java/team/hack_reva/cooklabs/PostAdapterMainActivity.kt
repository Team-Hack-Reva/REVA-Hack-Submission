package team.hack_reva.cooklabs

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.dynamiclinks.ktx.component1
import com.google.firebase.dynamiclinks.ktx.component2
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import team.hack_reva.cooklabs.LauncherActivity.Companion.nameOfuserOfTheApp
import team.hack_reva.cooklabs.LauncherActivity.Companion.reference_path_to_play_the_lab
import java.io.File

class PostAdapterMainActivity(private var postsList:List<Posts>):
RecyclerView.Adapter<PostAdapterMainActivity.MyViewHolder>(){
    lateinit var context:Context
    lateinit var progressDialogRecyclerView:ProgressDialog
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
        holder.profileimage.setImageResource(R.drawable.ic_baseline_account_circle_24)
        holder.backgroundImage.setImageResource(R.drawable.picsart_11_05_07_51_54)
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

        holder.itemView.setOnClickListener {
            val array = mutableListOf<String>(post.heading_text,post.reference,post.uid_of_author,post.author,post.user_pic_url).toString()
            reference_path_to_play_the_lab = "https://momtouch-cookbooks.com/array=$array"
            context.startActivity(Intent(context, PlayCookLabs::class.java))
        }
        holder.itemView.setOnLongClickListener{
            progressDialogRecyclerView = ProgressDialog(context)
            progressDialogRecyclerView.setMessage("Generating the link for the post, selected.")
            progressDialogRecyclerView.show()
            // Here i need to create a json data make is serialisable and send it to gen dynamic array
            val array = mutableListOf<String>(post.heading_text,post.reference,post.uid_of_author,post.author,post.user_pic_url).toString()
            GenerateDynamicLink("https://momtouch-cookbooks.com/array=$array")
            true
        }

    }
    override fun getItemCount(): Int {
        return postsList.size
    }

    private fun GenerateDynamicLink(reference_path:String){
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(reference_path)
            domainUriPrefix = "https://cooklabs.page.link"
            // Open links with this app on Android
            androidParameters { }
            // Open links with com.example.ios on iOS
        }
        val dynamicLinkUri = dynamicLink.uri
        Log.d("TAGLONGPRESS", "Long link generation : "+dynamicLink.toString())
        progressDialogRecyclerView.setMessage("Dynamic link generated. Now, trying to shorten the link...")
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            longLink = Uri.parse(dynamicLinkUri.toString())
        }.addOnSuccessListener { (shortLink, flowChartLink) ->
            // You'll need to import com.google.firebase.dynamiclinks.ktx.component1 and
            // com.google.firebase.dynamiclinks.ktx.component2
            Log.d("TAGLONGPRESS", "Short link generation : "+shortLink.toString())
            SendUrlText(shortLink.toString())
            progressDialogRecyclerView.dismiss()

        }.addOnFailureListener {
            // Error
            // ...
            progressDialogRecyclerView.dismiss()
            Log.e("ERROR", it.toString())
            Toast.makeText(context, "Error in generating the link. Please try again.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun SendUrlText(url:String){
        val string = "Hey, I am $nameOfuserOfTheApp.\n\n" +
                " I am enjoying Mom's Touch-Cookbooks App." +
                    "I have shared a cook lab with you. Find the link here and prepare a great dish. $url.\n\n " +
                "Haven't installed the app yet?. No issues just click on the above link to download the app." +
                    "You can also download the app from github releases.\nhttps://github.com/Team-Hack-Reva/REVA-Hack-Submission/releases/tag/1.0.1"

            val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, string)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

}

data class Posts(var user_pic_url:String, var background_pic_url:String, var heading_text:String, var reference:String, var step_count:Double,var author:String, var uid_of_author:String)