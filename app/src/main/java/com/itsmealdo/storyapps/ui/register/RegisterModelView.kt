package com.itsmealdo.storyapps.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.itsmealdo.storyapps.data.remote.AppModule
import com.itsmealdo.storyapps.data.repo.UsersRepository

class RegisterModelView (private val usersRepository: UsersRepository) : ViewModel() {
    fun registerUsers(name: String, email: String, password: String) = usersRepository.register(name, email, password)

    class RegisterModelViewFactory private constructor(private val usersRepository: UsersRepository) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterModelView::class.java)) {
                return RegisterModelView(usersRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: RegisterModelViewFactory? = null

            fun getInstance(): RegisterModelViewFactory = instance ?: synchronized(this) {
                instance ?: RegisterModelViewFactory(AppModule.provideUserRepository())
            }
        }
    }
}