package com.itsmealdo.storyapps.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.itsmealdo.storyapps.R
import com.itsmealdo.storyapps.data.remote.model.Result
import com.itsmealdo.storyapps.databinding.ActivityRegisterBinding
import com.itsmealdo.storyapps.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val registerModelView: RegisterModelView by viewModels {
        RegisterModelView.RegisterModelViewFactory.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewSetup()
    }

    private fun viewSetup() {
        supportActionBar?.hide()
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text
            val email = binding.etEmail.text
            val password = binding.etPassword.text
            if (!name.isNullOrEmpty() && !email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                val result = registerModelView.registerUsers(name.toString(), email.toString(), password.toString())
                result.observe(this) {
                    when (it) {
                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
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
            } else {
                if (name.isNullOrEmpty()) binding.etName.error = getString(R.string.name_empty)
                if (email.isNullOrEmpty()) binding.etEmail.error = getString(R.string.email_empty)
                if (password.isNullOrEmpty()) binding.etPassword.error = getString(R.string.password_empty)
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}