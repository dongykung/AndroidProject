package com.example.snsandroid.Navigation

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.snsandroid.HomeActivity
import com.example.snsandroid.MainActivity
import com.example.snsandroid.R
import com.example.snsandroid.model.ContentDTO
import com.example.snsandroid.model.FollowDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_user.view.*
class UserFragment : Fragment(){
    var fragmentView:View?=null

    lateinit var db: FirebaseFirestore
    var uid:String?=null
    var auth:FirebaseAuth?=null
    var currentUserUid:String?=null
    companion object{
        var PICK_PROFILE_FROM_ALBUM=10
    }
    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageprofileListenerRegistration: ListenerRegistration? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        db= Firebase.firestore
        uid=arguments?.getString("destinationUid")
        auth= FirebaseAuth.getInstance()
        currentUserUid= Firebase.auth.currentUser?.uid


        val filterActivityLauncher2: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if(it.resultCode == AppCompatActivity.RESULT_OK && it.data !=null) {
                    var photoUri = it.data?.data
                    var uid=FirebaseAuth.getInstance().currentUser?.uid
                    var stoargeRef=FirebaseStorage.getInstance().reference.child("userprofileImages").child(uid!!)
                    stoargeRef.putFile(photoUri!!).continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
                            return@continueWithTask stoargeRef.downloadUrl
                    }.addOnSuccessListener { uri->
                        var map=HashMap<String,Any>()
                        map["image"]=uri.toString()
                        FirebaseFirestore.getInstance().collection("profileImages").document(uid!!).set(map)
                    }
                } else if(it.resultCode == AppCompatActivity.RESULT_CANCELED){

                }else{
                    Log.d("ActivityResult","something wrong")
                }
            }

        if(uid==currentUserUid){
            fragmentView?.account_btn_follow_signout?.text=context?.getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener{
                activity?.finish()
                startActivity(Intent(activity,MainActivity::class.java))
                auth?.signOut()
            }
            fragmentView?.account_iv_profile?.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("image/*")
                filterActivityLauncher2.launch(intent)
                getProfileImage()
            }
        }
        else{
                fragmentView?.account_btn_follow_signout?.text=context?.getString(R.string.follow)
                var mainactivity=(activity as HomeActivity)
                mainactivity.toolbar_username?.text=arguments?.getString("userId")
                mainactivity.toolbar_btn_back?.setOnClickListener{
                    mainactivity.bottom_navigation.selectedItemId=R.id.action_home
                }
            mainactivity.toolbar_title_image.visibility=View.GONE
            mainactivity.toolbar_username.visibility=View.VISIBLE
            mainactivity.toolbar_btn_back.visibility=View.VISIBLE

            fragmentView?.account_btn_follow_signout?.setOnClickListener{
                requestFollow()
            }
        }

        getFollowing()
        getFollower()
        fragmentView?.account_recyclerview?.adapter=UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager=GridLayoutManager(requireActivity(),3)
        getProfileImage()

        return fragmentView
    }


    fun getFollowing() {
        followingListenerRegistration = db.collection("users").document(uid!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView!!.account_tv_following_count.text = followDTO?.followingCount.toString()
        }
    }
     fun getFollower() {

        followListenerRegistration = db.collection("users")?.document(uid!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount.toString()
            if (followDTO?.followers?.containsKey(currentUserUid)!!) {

                fragmentView?.account_btn_follow_signout?.text = context?.getString(R.string.follow_cancel)
                fragmentView?.account_btn_follow_signout
                    ?.background
                    ?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
            } else {

                if (uid != currentUserUid) {

                    fragmentView?.account_btn_follow_signout?.text = context?.getString(R.string.follow)
                    fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                }
            }

        }

    }

    fun requestFollow() {


        var tsDocFollowing = db!!.collection("users").document(currentUserUid!!)
        db.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction

            }
            // Unstar the post and remove self from stars
            if (followDTO.followings.containsKey(uid)!!) {
                if(followDTO.followingCount>=1) {
                    followDTO.followingCount = followDTO.followingCount - 1
                    followDTO.followings.remove(uid)
                }
            } else {

                followDTO.followingCount = followDTO.followingCount + 1
                followDTO.followings[uid!!] = true

            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        var tsDocFollower = db!!.collection("users").document(uid!!)
        db.runTransaction { transaction ->

            var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)
            if (followDTO == null) {

                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true


                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO?.followers?.containsKey(currentUserUid!!)!!) {

                if(followDTO!!.followerCount>=1) {
                    followDTO!!.followerCount = followDTO!!.followerCount - 1
                    followDTO!!.followers.remove(currentUserUid!!)
                }
            } else {

                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true

            }// Star the post and add self to stars

            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }

    }
    fun getProfileImage() {
        imageprofileListenerRegistration = db.collection("profileImages").document(uid!!)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.data != null) {
                    val url = documentSnapshot?.data!!["image"]
                    Glide.with(requireActivity())
                        .load(url)
                        .apply(RequestOptions().circleCrop()).into(fragmentView!!.account_iv_profile)
                }
            }

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

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        @SuppressLint("CheckResult")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview=(holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
           return contentDTOs.size
        }

    }
    override fun onStop() {
        super.onStop()
        followListenerRegistration?.remove()
        followingListenerRegistration?.remove()

    }
    override fun onResume() {
        super.onResume()
        getProfileImage()
    }
}