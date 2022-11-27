package com.example.snsandroid.Navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.snsandroid.R
import com.example.snsandroid.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_user.view.*
class UserFragment : Fragment(){
    var fragmentView:View?=null
    lateinit var db: FirebaseFirestore
    var uid:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        db= Firebase.firestore
        return fragmentView
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class UserFragmentRecyclerViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs:ArrayList<ContentDTO>  = arrayListOf()
        init{
            db.collection("images").whereEqualTo("uid",uid).addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                if(querySnapshot==null) return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                fragmentView?.account_tv_post_count?.text=contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                var width=resources.displayMetrics.widthPixels /3
                var imageview= ImageView(parent.context)
                imageview.layoutParams=LinearLayoutCompat.LayoutParams(width,width)
                return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

    }
}