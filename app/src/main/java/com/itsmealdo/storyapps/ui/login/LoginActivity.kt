package com.itsmealdo.storyapps.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itsmealdo.storyapps.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
    }
}