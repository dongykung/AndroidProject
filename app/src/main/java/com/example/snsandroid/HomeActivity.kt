package com.example.snsandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.snsandroid.navigation.AlarmFragment
import com.example.snsandroid.navigation.DetailViewFragment
import com.example.snsandroid.navigation.GridFragment
import com.example.snsandroid.navigation.*
import com.example.snsandroid.databinding.ActivityAddPhotoBinding
import com.example.snsandroid.databinding.ActivityHomeBinding
import com.example.snsandroid.navigation.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val detailViewFragment= DetailViewFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home ->{
                    val detailViewFragment= DetailViewFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()

                    return@setOnItemSelectedListener true
                }
                R.id.action_search ->{
                    val gridFragment= GridFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()

                    return@setOnItemSelectedListener true
                }
                R.id.action_add_photo ->{
                    println("사진추가")
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(this, AddPhotoAcitivty::class.java))
                    } else {
                        Toast.makeText(this, "스토리지 읽기 권한이 없습니다.", Toast.LENGTH_LONG).show()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.action_favorite_alarm ->{
                    val alarmFragment= AlarmFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.action_account ->{
                    val userFragment= UserFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid",uid)
                    userFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                    return@setOnItemSelectedListener true
                }

            }
            false
        }

    }

    fun setToolbarDefault() {
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }

}