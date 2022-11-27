package com.example.snsandroid.Navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.snsandroid.databinding.ActivityAddPhotoBinding
import com.example.snsandroid.model.ContentDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*


class AddPhotoAcitivty : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding
    lateinit var storage: FirebaseStorage
    lateinit var db: FirebaseFirestore
    var photoUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        storage = Firebase.storage
        db=Firebase.firestore

        setContentView(binding.root)
        val filterActivityLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if(it.resultCode == RESULT_OK && it.data !=null) {
                     photoUri = it.data?.data
                    try {
                        photoUri?.let {
                            if(Build.VERSION.SDK_INT < 28) {
                                val bitmap = MediaStore.Images.Media.getBitmap(
                                    this.contentResolver,
                                    photoUri
                                )
                                binding.addphotoImage.setImageBitmap(bitmap)
                            } else {
                                val source = ImageDecoder.createSource(this.contentResolver, photoUri!!)
                                val bitmap = ImageDecoder.decodeBitmap(source)
                                binding.addphotoImage.setImageBitmap(bitmap)
                            }
                        }


                    }catch(e:Exception) {
                        e.printStackTrace()
                    }
                } else if(it.resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                }else{
                    Log.d("ActivityResult","something wrong")
                }
            }
    binding.addphotoImage.setOnClickListener{
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        filterActivityLauncher.launch(intent)
    }
    binding.addphotoBtnUpload.setOnClickListener{
        uploadContent()
    }
    }

    @SuppressLint("SimpleDateFormat")
    fun uploadContent(){
        var timestamp=SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName="IMAGE"+timestamp+"_.png"

        var storageRef=storage.reference.child("images").child(imageFileName)

        storageRef.putFile(photoUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {   uri->
                var contentDTO=ContentDTO()
                contentDTO.imageUrl=uri.toString()
                contentDTO.uid= Firebase.auth.currentUser?.uid
                contentDTO.userId=Firebase.auth.currentUser?.email
                contentDTO.explain=binding.addphotoEditExplain.text.toString()
                contentDTO.timestamp=System.currentTimeMillis()
                db.collection("images").document().set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

}