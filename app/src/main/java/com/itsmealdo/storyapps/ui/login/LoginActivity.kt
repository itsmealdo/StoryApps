package com.itsmealdo.storyapps.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.itsmealdo.storyapps.databinding.ActivityLoginBinding
import com.itsmealdo.storyapps.ui.main.MainActivity
import com.itsmealdo.storyapps.ui.register.RegisterActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val loginModelView: LoginModelView by viewModels {
        LoginModelView.loginModelViewFactory.getInstance(
            UsersPreferences.getInstance(dataStore)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewSetup()
    }

    private fun viewSetup() {
        supportActionBar?.hide()

        binding.btnLogin.setOnClickListener {
            if (!binding.etEmail.text.isNullOrEmpty() && !binding.etPassword.text.isNullOrEmpty()) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val result = loginModelView.login(email, password)

                if (password.length <= 7) {
                    Toast.makeText(this, getString(R.string.password_minimum), Toast.LENGTH_SHORT).show()
                } else {
                    result.observe(this) {
                        when (it) {
                            is Result.Success -> {
                                binding.progressBar.visibility = View.GONE
                                val data = it.data
                                loginModelView.saveAuthToken(data.loginResult.token)
                                Log.d("LoginActivity", "token: ${data.loginResult.token}")
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
            } else {
                    binding.etEmail.error = resources.getString(R.string.email_empty)
                    binding.etPassword.error = resources.getString(R.string.password_empty)
                }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}