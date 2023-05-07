package com.itsmealdo.storyapps.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.itsmealdo.storyapps.databinding.ActivityDetailBinding
import com.itsmealdo.storyapps.ui.main.MainActivity
import com.itsmealdo.storyapps.utils.DateFormatter
import java.util.TimeZone

class DetailActivity : AppCompatActivity() {

    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewSetup()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun viewSetup() {
        val name = intent.getStringExtra(NAME_EXTRA)
        val createdAt = intent.getStringExtra(CREATED_AT_EXTRA)
        val description = intent.getStringExtra(DESCRIPTION_EXTRA)
        val imageUrl = intent.getStringExtra(IMAGE_URL_EXTRA)

        supportActionBar?.hide()

        binding.tvUsername.text = name
        binding.tvUsernames.text = name
        binding.tvCreatedAt.text = createdAt?.let { DateFormatter.formatDate(it, TimeZone.getDefault().id) }
        binding.tvDesc.text = description

        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivStory)

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    companion object {
        const val NAME_EXTRA = "name_extra"
        const val CREATED_AT_EXTRA = "created_at_extra"
        const val DESCRIPTION_EXTRA = "description_extra"
        const val IMAGE_URL_EXTRA = "img_extra"
    }
}