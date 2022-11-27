package com.example.snsandroid.Navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.snsandroid.HomeActivity
import com.example.snsandroid.R
import com.example.snsandroid.databinding.ActivityCommentBinding

import com.example.snsandroid.databinding.ActivityHomeBinding
import com.example.snsandroid.model.AlarmDTO
import com.example.snsandroid.model.ContentDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*


class CommentActivity: AppCompatActivity() {
    var contentUid:String?=null
    var destinationUid:String?=null
    private lateinit var binding: ActivityCommentBinding
    lateinit var db: FirebaseFirestore
    var commentSnapshot: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contentUid=intent.getStringExtra("contentUid")
        destinationUid= intent.getStringExtra("destinationUid")
        db= Firebase.firestore

        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        binding.commentBtnSend.setOnClickListener{
            var comment= ContentDTO.Comment()
            comment.userId= Firebase.auth.currentUser?.email
            comment.uid=Firebase.auth.currentUser?.uid
            comment.comment=binding.commentEditMessage.text.toString()
            comment.timestamp=System.currentTimeMillis()

            db.collection("images").document(contentUid!!).collection("comments").document().set(comment)
            commentAlarm(destinationUid!!,binding.commentEditMessage.text.toString())
            binding.commentEditMessage.setText("")
        }
    }
    fun commentAlarm(destination:String,message:String){
        var alarmDTO=AlarmDTO()
        alarmDTO.destinationUid=destination
        alarmDTO.userId=Firebase.auth.currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid=Firebase.auth.currentUser?.uid
        alarmDTO.timestamp=System.currentTimeMillis()
        alarmDTO.message=message
        db.collection("alarms").document().set(alarmDTO)
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()
        init{
            commentSnapshot = db.collection("images").document(contentUid!!) .collection("comments").orderBy("timestamp",
                Query.Direction.DESCENDING)
                .addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentviewitem_textview_comment.text=comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId

            db.collection("profileImages").document(comments[position].uid!!)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot?.data != null) {

                        val url = documentSnapshot?.data!!["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                    }
                }

        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}