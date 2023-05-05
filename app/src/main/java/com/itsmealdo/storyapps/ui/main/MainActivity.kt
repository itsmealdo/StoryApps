package com.itsmealdo.storyapps.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.itsmealdo.storyapps.data.adapter.StoryAdapter
import com.itsmealdo.storyapps.data.local.datastore.UsersPreferences
import com.itsmealdo.storyapps.data.remote.model.Result
import com.itsmealdo.storyapps.databinding.ActivityMainBinding
import com.itsmealdo.storyapps.ui.camera.CameraActivity
import com.itsmealdo.storyapps.ui.login.LoginActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainModelView: MainModelView by viewModels {
        MainModelView.MainModelViewFactory.getInstance(
            this,
            UsersPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupButton()
    }

    override fun onResume() {
        super.onResume()
        checkSession()
    }

    private fun checkSession() {
        mainModelView.checkToken().observe(this) {
            if (it == "null") {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                viewSetup("Bearer $it")
            }
        }

    }

    private fun viewSetup(token: String) {
        supportActionBar?.hide()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnLogout.setOnClickListener {
            mainModelView.logout()
            finish()
        }

        mainModelView.getUserStory(token).observe(this) {
            when (it) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = it.data
                    if (data.isEmpty()) {
                        binding.tvNoStory.visibility = View.VISIBLE
                    } else {
                        binding.tvNoStory.visibility = View.GONE
                        binding.rvStory.apply {
                            adapter = StoryAdapter(this@MainActivity, data)
                            layoutManager = LinearLayoutManager(this@MainActivity)
                        }
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    val error = it.error
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupButton() {
        binding.btnAddPost.setOnClickListener {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}