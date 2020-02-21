package com.demo.chatie.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.demo.chatie.R
import com.demo.chatie.login.view.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        screen.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }
}
