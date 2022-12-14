package com.example.snsandroid.Navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.snsandroid.R
import com.example.snsandroid.model.AlarmDTO
import com.example.snsandroid.model.ContentDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){
    lateinit var db: FirebaseFirestore
    var uid:String?=null
    var title:String?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view= LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        db= Firebase.firestore
        uid=Firebase.auth.currentUser?.uid
        view.detailviewfragment_recyclerview.adapter=DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager=LinearLayoutManager(activity)
        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class DetailViewRecyclerViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs:ArrayList<ContentDTO>  = arrayListOf()
        var contentUidList:ArrayList<String> = arrayListOf()
        var contentUserList:ArrayList<String> = arrayListOf()
        init{
            db.collection("images").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(querySnapshot==null) return@addSnapshotListener

                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item=snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        db.collection("users").addSnapshotListener{querySnapshot, firebaseFirestoreException ->
            if(querySnapshot==null) return@addSnapshotListener
            contentUserList.clear()
            for(snapshot in querySnapshot!!.documents){
                contentUserList.add(snapshot.id)
            }
            notifyDataSetChanged()
        }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view=LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder=(holder as CustomViewHolder).itemView
            //userid
            viewholder.detailviewitem_profile_textview.text=contentDTOs!![position].userId
            //image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_imageview_content)
            //ex content
            viewholder.detailviewitem_explain_textview.text=contentDTOs!![position].explain
            //?????????
            viewholder.detailviewitem_favoritecounter_textview.text="????????? "+contentDTOs!![position].favoriteCount
            //????????? ?????????


            db.collection("profileImages").document(contentDTOs[position].uid!!)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val url = task.result["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop()).into(viewholder.detailviewitem_profile_image)

                    }
                }
            //????????? ?????? ?????????
            viewholder.detailviewitem_favorite_imageview.setOnClickListener{
                favorite(position)
            }

            //????????? ??????
            if(contentDTOs!![position].favorites.containsKey(uid)){
                    viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.heart)
            }else{
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.love)
            }
            viewholder.detailviewitem_profile_image.setOnClickListener{
                var fragment=UserFragment()
                var bundle=Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments=bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }

            viewholder.detailviewitem_comment_imageview.setOnClickListener{
                var intent= Intent(activity, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid",contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        fun favorite(position:Int){
            var tsDoc=db.collection("images").document(contentUidList[position])
            db.runTransaction{ transaction->

                var contentDTO=transaction.get(tsDoc).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                            contentDTO.favoriteCount = contentDTO.favoriteCount -1
                            contentDTO.favorites.remove(uid)
                }else{
                    contentDTO.favoriteCount = contentDTO.favoriteCount +1
                    contentDTO.favorites[uid!!]=true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc,contentDTO)
            }
        }
        fun favoriteAlarm(destinationUid:String){
            var alarmDTO=AlarmDTO()
            alarmDTO.destinationUid=destinationUid
            alarmDTO.userId=Firebase.auth.currentUser?.email
            alarmDTO.uid=Firebase.auth.currentUser?.uid
            alarmDTO.kind=0
            alarmDTO.timestamp=System.currentTimeMillis()
            db.collection("alarms").document().set(alarmDTO)
        }

    }
}