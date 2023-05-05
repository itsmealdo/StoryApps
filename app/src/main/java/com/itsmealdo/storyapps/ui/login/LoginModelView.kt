package com.itsmealdo.storyapps.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.itsmealdo.storyapps.data.local.datastore.UsersPreferences
import com.itsmealdo.storyapps.data.remote.AppModule
import com.itsmealdo.storyapps.data.repo.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginModelView(
    private val usersRepository: UsersRepository,
    private val usersPreferences: UsersPreferences
) : ViewModel() {

    fun login(email: String, password: String) = usersRepository.login(email, password)
    fun register(name: String, email: String, password: String) = usersRepository.register(name, email, password)
    fun saveAuthToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            usersPreferences.saveUserToken(token)
        }
    }

    fun checkFirstTime(): LiveData<Boolean> {
        return usersPreferences.isFirstTime().asLiveData()
    }

    class loginModelViewFactory private constructor(
        private val usersRepository: UsersRepository,
        private val usersPreferences: UsersPreferences
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) : T {
            if(modelClass.isAssignableFrom(LoginModelView::class.java)) {
                return LoginModelView(usersRepository, usersPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: loginModelViewFactory? = null
            fun getInstance(
                usersPreferences: UsersPreferences
            ): loginModelViewFactory =
                instance ?: synchronized(this) {
                    instance ?: loginModelViewFactory(
                        AppModule.provideUserRepository(),
                        usersPreferences
                    )
                }
        }
    }

}