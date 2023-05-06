package com.itsmealdo.storyapps.ui.post

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.itsmealdo.storyapps.R
import com.itsmealdo.storyapps.data.local.datastore.UsersPreferences
import com.itsmealdo.storyapps.data.remote.model.Result
import com.itsmealdo.storyapps.databinding.ActivityPostBinding
import com.itsmealdo.storyapps.ui.login.LoginActivity
import com.itsmealdo.storyapps.ui.main.MainActivity
import com.itsmealdo.storyapps.utils.reduceFileImage
import com.itsmealdo.storyapps.utils.rotateBitmap
import com.itsmealdo.storyapps.utils.storyExecutor
import java.io.File
import java.io.FileOutputStream

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class PostActivity : AppCompatActivity() {

    private val binding: ActivityPostBinding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }

    private val storyExecutor: storyExecutor by lazy {
        storyExecutor()
    }

    private val postModelView: PostModelView by viewModels {
        PostModelView.PostModelViewFactory.getInstance(
            this, UsersPreferences.getInstance(dataStore)
        )
    }

    private var file: File? = null
    private var isBack: Boolean = true
    private var hasReducingDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        bindResults()
        buttonSetup()
    }

    override fun onResume() {
        super.onResume()
        checkSession()
    }

    private fun checkSession() {
        postModelView.checkTokenAvailable().observe(this) {
            if (it == "null") {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    private fun bindResults() {
        file = intent.getSerializableExtra(PHOTOS_RESULT_EXTRA) as File
        isBack = intent.getBooleanExtra(IS_BACK_EXTRA, true)

        val results = rotateBitmap(BitmapFactory.decodeFile((file as File).path), isBack)
        results.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))

        storyExecutor.diskIO.execute {
            file = reduceFileImage(file as File)
            hasReducingDone = true
        }
        binding.ivStory.setImageBitmap(results)
    }

    private fun buttonSetup() {
        binding.btnPost.setOnClickListener {
            postModelView.checkTokenAvailable().observe(this) {
                if (hasReducingDone) {
                    if (it == "null") {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        uploadImg("Bearer $it")
                    }
                } else {
                    Toast.makeText(this, getString(R.string.wait_processing), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImg(token: String) {
        if (binding.etDesc.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.desc_required), Toast.LENGTH_SHORT).show()
        } else {
            if (file != null) {
                binding.progressBar.visibility = View.VISIBLE
                val desc = binding.etDesc.text.toString()
                val results = postModelView.postUserStory(token, file as File, desc)
                results.observe(this) {
                    when (it) {
                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, getString(R.string.post_success), Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            val error = it.error
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        }

                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    companion object {
        const val PHOTOS_RESULT_EXTRA = "photos_result_extra"
        const val IS_BACK_EXTRA = "is_back_extra"
    }




}