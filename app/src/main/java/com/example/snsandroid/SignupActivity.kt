package com.example.snsandroid

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snsandroid.databinding.ActivitySignupBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signupbtn2.setOnClickListener{
            var userEmail=binding.emailsignup.text.toString()
            val password = binding.passwordsignup.text.toString()
            signUp(userEmail,password)
            binding.progress.visibility= View.VISIBLE
        }
    }
    private fun signUp(userEmail: String, password: String){
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    Toast.makeText(this, "축하합니다 회원가입 완료.", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility= View.GONE
                    finish()
                }else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    binding.progress.visibility= View.GONE
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }

    }
}