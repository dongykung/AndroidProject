package com.example.snsandroid.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snsandroid.R
import com.example.snsandroid.model.ContentDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){
    lateinit var db: FirebaseFirestore
    var uid:String?=null
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
        init{
            db.collection("images").orderBy("timestamp").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item=snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
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
            //좋아요
            viewholder.detailviewitem_favoritecounter_textview.text="좋아요 "+contentDTOs!![position].favoriteCount
            //프로필 이미지
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_profile_image)
            //좋아요 클릭 리스너
            viewholder.detailviewitem_favorite_imageview.setOnClickListener{
                favorite(position)
            }

            //좋아요 하트
            if(contentDTOs!![position].favorites.containsKey(uid)){
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.heart)
            }else{
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.love)
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
                }
                transaction.set(tsDoc,contentDTO)
            }
        }

    }
}