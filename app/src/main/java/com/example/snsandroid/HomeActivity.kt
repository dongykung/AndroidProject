package com.example.snsandroid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.snsandroid.databinding.ActivityHomeBinding
import com.example.snsandroid.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class HomeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}