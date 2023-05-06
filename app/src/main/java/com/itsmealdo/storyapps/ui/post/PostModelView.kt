package com.itsmealdo.storyapps.ui.post

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.itsmealdo.storyapps.data.local.datastore.UsersPreferences
import com.itsmealdo.storyapps.data.remote.AppModule
import com.itsmealdo.storyapps.data.repo.StoryRepository
import java.io.File

class PostModelView(
    private val storyRepository: StoryRepository,
    private val usersPreferences: UsersPreferences
) : ViewModel() {
    fun postUserStory(token:String, imageFile: File, description: String) =
        storyRepository.postUserStory(token, imageFile, description)

    fun checkTokenAvailable(): LiveData<String> {
        return usersPreferences.getUserToken().asLiveData()
    }

    class PostModelViewFactory private constructor(
        private val storyRepository: StoryRepository,
        private val usersPreferences: UsersPreferences
    ) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostModelView::class.java)) {
                return PostModelView(storyRepository, usersPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Model class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: PostModelViewFactory? = null

            fun getInstance(
                context: Context,
                usersPreferences: UsersPreferences
            ): PostModelViewFactory =
                instance ?: synchronized(this) {
                    instance ?: PostModelViewFactory(
                        AppModule.provideStoryRepository(context),
                        usersPreferences
                    )
                }.also { instance = it }
        }
    }
}
