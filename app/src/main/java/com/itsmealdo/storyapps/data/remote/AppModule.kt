package com.itsmealdo.storyapps.data.remote

import android.content.Context
import com.itsmealdo.storyapps.data.local.room.StoryDatabase
import com.itsmealdo.storyapps.data.remote.network.ApiConfig
import com.itsmealdo.storyapps.data.repo.StoryRepository
import com.itsmealdo.storyapps.data.repo.UsersRepository
import com.itsmealdo.storyapps.utils.storyExecutor

object AppModule {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val storyDao = database.storyDao()
        val apiService = ApiConfig.getApiServices()
        val storyExecutor = storyExecutor()
        return StoryRepository.getInstance(storyDao, apiService, storyExecutor)
    }

    fun provideUserRepository(): UsersRepository {
        val apiService = ApiConfig.getApiServices()
        return UsersRepository.getInstance(apiService)
    }
}