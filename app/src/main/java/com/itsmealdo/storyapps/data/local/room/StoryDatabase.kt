package com.itsmealdo.storyapps.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itsmealdo.storyapps.data.local.entity.StoryList


@Database(entities = [StoryList::class], version = 1)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao() : StoryDao

    companion object {
        @Volatile
        private var instance: StoryDatabase? = null

        fun getInstance(context: Context) : StoryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, StoryDatabase::class.java, "stories.db")
                    .build()
            }.also { instance = it }
    }
}