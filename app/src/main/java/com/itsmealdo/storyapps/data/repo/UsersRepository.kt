package com.itsmealdo.storyapps.data.repo

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.itsmealdo.storyapps.data.remote.model.LoginResponse
import com.itsmealdo.storyapps.data.remote.model.RegisterResponse
import com.itsmealdo.storyapps.data.remote.model.Result
import com.itsmealdo.storyapps.data.remote.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersRepository private constructor(
    private val apiService: ApiService
) {
    private val userLoginResult = MutableLiveData<Result<LoginResponse>>()
    private val userRegisterResult = MutableLiveData<Result<RegisterResponse>>()

    fun register(name: String, email: String, password: String): MutableLiveData<Result<RegisterResponse>> {
        userRegisterResult.value = Result.Loading
        val client = apiService.UserRegister(
            name,
            email,
            password
        )

        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerBody = response.body()
                    if (registerBody != null) {
                        userRegisterResult.value = Result.Success(registerBody)
                    } else {
                        userRegisterResult.value = Result.Error(REGISTER_MESSAGE_ERROR)
                        Log.e(TAG, "Failed: Register info is null")
                    }
                } else {
                    userRegisterResult.value = Result.Error(REGISTER_MESSAGE_ERROR)
                    Log.e(TAG, "Failed: Response Unsuccessful - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                userRegisterResult.value = Result.Error(REGISTER_MESSAGE_ERROR)
                Log.e(TAG, "Failed: Response Failure - ${t.message.toString()}")
            }
        })

        return userRegisterResult
    }

    fun login(email: String, password: String): MutableLiveData<Result<LoginResponse>> {
    userLoginResult.value = Result.Loading
        val client = apiService.UserLogin(
            email,
            password
        )

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginBody = response.body()
                    if (loginBody != null) {
                        userLoginResult.value = Result.Success(loginBody)
                    } else {
                        userLoginResult.value = Result.Error(LOGIN_MESSAGE_ERROR)
                        Log.e(TAG, "Failed: Login data is null")
                    }
                } else {
                    userLoginResult.value = Result.Error(LOGIN_MESSAGE_ERROR)
                    Log.e(TAG, "Failed: Response Unsuccessful - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                userLoginResult.value = Result.Error(LOGIN_MESSAGE_ERROR)
                Log.e(TAG, "Failed: Response Failure - ${t.message.toString()}")
            }
        })

        return userLoginResult
    }

    companion object {
        private val TAG = UsersRepository::class.java.simpleName
        private const val LOGIN_MESSAGE_ERROR = "Your email or password is incorrect, please try again. "
        private const val REGISTER_MESSAGE_ERROR = "Register failed, please try again later."

        @Volatile
        private var instance: UsersRepository? = null

        fun getInstance(apiService: ApiService) =
            instance ?: synchronized(this) {
                instance ?: UsersRepository(apiService)
            }.also { instance = it }
    }
}