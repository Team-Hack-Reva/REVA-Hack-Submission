package team.hack_reva.cooklabs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my__creations_.*


class My_Creations_Fragment : Fragment() {
    private val posts_list = ArrayList<Posts>()
    lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val authuid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val view =  inflater.inflate(R.layout.fragment_my__creations_, container, false)
        val postsAdapter = PostAdapterMainActivity(posts_list)
        recyclerView = view.findViewById(R.id.recycler_view_creations) as RecyclerView
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = postsAdapter

        val photo_storage = FirebaseStorage.getInstance().reference
        val firestore = FirebaseFirestore.getInstance().collection("user-cook-labs")
            .document(authuid)
            .collection("posts")
            .get().addOnCompleteListener {
                if(!it.result?.isEmpty!!){
                    for (i in it.result!!.documents){
                        // First is to get the data
                        Log.d("DATA", "Data is coming boyui")
                        val storage_path = i.getString("audio-storage-name").toString()
                        val author = i.getString("author").toString()
                        val name_of_dish = i.getString("name-of-post").toString()
                        val no_of_steps = i.getDouble("no-of-steps")?.toDouble()
                        var cover_pic_path = i.getString("cover-picture-path").toString()
                        cover_pic_path = cover_pic_path.replace(".jpg", "")
                        val uid_of_author = i.getString("uid-of-author").toString()
                        Log.d("TEST", cover_pic_path)
                        // Then download the necessary items to show it up there in the recyclerview
                        var url_of_cover_pic = ""
                        photo_storage.child(cover_pic_path).downloadUrl.addOnFailureListener {
                            Log.d("DOWNLOAD", "Unable to download as it failed in downloading images")
                        }.addOnSuccessListener {
                            url_of_cover_pic = it.toString()
                        }
                        Log.d("CHECK", "Url of cover pic $url_of_cover_pic")
                        var url_of_profile_pic = ""
                        val post_pic = FirebaseStorage.getInstance().reference
                        post_pic.child("user-accounts/profile-pictures/$uid_of_author").downloadUrl.addOnSuccessListener {
                            url_of_profile_pic = it.toString()
                            posts_list.add(Posts(url_of_profile_pic,url_of_cover_pic,name_of_dish,"user-cook-labs/$authuid/posts/$name_of_dish"))
                            postsAdapter.notifyDataSetChanged()
                        }


                    }
                }else{
                    Toast.makeText(context,"You will have to create a lab inorder to see them in my creations section", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Log.d("TAG", "There is an exception in the fire store reading the data")
            }



        postsAdapter.notifyDataSetChanged()
        return view
    }


}