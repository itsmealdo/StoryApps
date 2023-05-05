package com.itsmealdo.storyapps.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.itsmealdo.storyapps.data.local.datastore.UsersPreferences
import com.itsmealdo.storyapps.data.remote.AppModule
import com.itsmealdo.storyapps.data.repo.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainModelView(
    private val storyRepository: StoryRepository,
    private val usersPreferences: UsersPreferences

) : ViewModel() {
    fun getUserStory(token: String) = storyRepository.getUserStory(token)

    fun checkToken(): LiveData<String> {
        return usersPreferences.getUserToken().asLiveData()
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            usersPreferences.deleteUserToken()
        }
    }

    class MainModelViewFactory private constructor(
        private val storyRepository: StoryRepository,
        private val usersPreferences: UsersPreferences
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(MainModelView::class.java)) {
                return MainModelView(storyRepository, usersPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: MainModelViewFactory? = null

            fun getInstance(
                context: Context,
                usersPreferences: UsersPreferences
            ): MainModelViewFactory = instance ?: synchronized(this) {
                instance ?: MainModelViewFactory(
                    AppModule.provideStoryRepository(context),
                    usersPreferences
                )
            }.also { instance = it }
        }
    }


}