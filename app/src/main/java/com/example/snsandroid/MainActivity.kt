package com.example.snsandroid


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snsandroid.databinding.ActivityMainBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.loginbtn.setOnClickListener{
            var userEmail=binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            binding.progress.visibility=View.VISIBLE
            doLogin(userEmail,password)
        }
        binding.signupbtn.setOnClickListener{
            var userEmail=binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            binding.progress.visibility=View.VISIBLE
            signUp(userEmail,password)
        }

    }
    fun moveMainPage(user: FirebaseUser?) {

        // User is signed in
        if (user != null) {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()

        }
    }
    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) { // it: Task<AuthResult!>
                if (it.isSuccessful) {
                    Toast.makeText(this, "Authentication suceses.", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility=View.GONE
                } else {
                    binding.progress.visibility=View.GONE
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUp(userEmail: String, password: String){
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    Toast.makeText(this, "축하합니다 회원가입 완료.", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility=View.GONE
                }else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    binding.progress.visibility=View.GONE
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }

    }


}