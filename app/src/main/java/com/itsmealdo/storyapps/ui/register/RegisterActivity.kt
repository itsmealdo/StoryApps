package com.itsmealdo.storyapps.ui.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itsmealdo.storyapps.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()
    }
}