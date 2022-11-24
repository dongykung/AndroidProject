package com.example.snsandroid

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.snsandroid.databinding.ActivityHomeBinding


class HomeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE
        binding.bottomNavigation.selectedItemId = R.id.action_home
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)


        binding.bottomNavigation.setOnItemSelectedListener {
           when(it.itemId){
               R.id.action_home ->{
                   println("잘될거야")

                   return@setOnItemSelectedListener true
               }
               R.id.action_search ->{
                   println("검색")

                   return@setOnItemSelectedListener true
               }
               R.id.action_add_photo ->{
                   println("사진추가")

                   return@setOnItemSelectedListener true
               }
               R.id.action_favorite_alarm ->{
                   println("좋아요")

                   return@setOnItemSelectedListener true
               }
               R.id.action_account ->{
                   println("계정")

                   return@setOnItemSelectedListener true
               }

                   }
             false
              }

        }

    fun setToolbarDefault() {
        binding.toolbarTitleImage.visibility = View.GONE
        binding.toolbarBtnBack.visibility = View.GONE
        binding.toolbarUsername.visibility = View.GONE
    }

}