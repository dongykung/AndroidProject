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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {

    var alarmSnapshot: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        view.alarmframgent_recyclerview.adapter = AlarmRecyclerViewAdapter()
        view.alarmframgent_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val alarmDTOList = ArrayList<AlarmDTO>()
        init {

            val uid = Firebase.auth.currentUser!!.uid
            println(uid)
            FirebaseFirestore.getInstance()
                .collection("alarms")
                .whereEqualTo("destinationUid", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOList.clear()
                    if(querySnapshot == null)return@addSnapshotListener
                    for (snapshot in querySnapshot.documents!!) {
                        alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                    }
                    alarmDTOList.sortByDescending { it.timestamp }
                    notifyDataSetChanged()
                }



        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val profileImage = holder.itemView.commentviewitem_imageview_profile
            val commentTextView = holder.itemView.commentviewitem_textview_profile

            FirebaseFirestore.getInstance().collection("profileImages")
                .document(alarmDTOList[position].uid!!).get().addOnCompleteListener {
                        task ->
                    if(task.isSuccessful){
                        val url = task.result["image"]
                        activity?.let {
                            Glide.with(it)
                                .load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(profileImage)
                        }
                    }
                }

            when (alarmDTOList[position].kind) {
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    commentTextView.text = str_0
                }

                1 -> {
                    val str_1 = alarmDTOList[position].userId + getString(R.string.alarm_who) + alarmDTOList[position].message + getString(R.string.alarm_comment)
                    commentTextView.text = str_1
                }

                2 -> {
                    val str_2 = alarmDTOList[position].userId + getString(R.string.alarm_follow)
                    commentTextView.text = str_2
                }
            }
            holder.itemView.setOnClickListener{
                var fragment=UserFragment()
                var bundle=Bundle()
                bundle.putString("destinationUid", alarmDTOList[position].uid)
                bundle.putString("userId", alarmDTOList[position].userId)
                fragment.arguments=bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }

        }

        override fun getItemCount(): Int {

            return alarmDTOList.size
        }
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    }
}