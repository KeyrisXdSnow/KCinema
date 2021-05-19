package com.example.kcinema.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.kcinema.MainActivity
import com.example.kcinema.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText

    private lateinit var login: Button
    private lateinit var register: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initView()
        auth = Firebase.auth
        initListener()

    }

    private fun initView() {
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.signIn)
        register = findViewById(R.id.signUp)
    }

    private fun initListener() {
        login.setOnClickListener {
            val email = username.text.toString()
            val pass = password.text.toString()
            signIn(email, pass)
        }
        register.setOnClickListener {
            val email = username.text.toString()
            val pass = password.text.toString()
            createAccount(email, pass)
        }
    }

    override fun onResume() {
        super.onResume()

        if (FirebaseAuth.getInstance().currentUser != null) {
            finish()
        }
    }


    private fun signIn(email: String, password: String) {

        if (email.isEmpty() || password.isEmpty()) return

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    this.let {
                        it.startActivity(Intent(it, MainActivity::class.java))
                    }

                } else { print("all bad =(") }
            }
    }

    private fun createAccount(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    this.let {
                        it.startActivity(Intent(it, MainActivity::class.java))
                    }
                } else { print("we don't create user =(") }
            }
    }
}
