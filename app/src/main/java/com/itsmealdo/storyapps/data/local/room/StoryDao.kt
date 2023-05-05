package com.itsmealdo.storyapps.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.itsmealdo.storyapps.data.local.entity.StoryList

@Dao
interface StoryDao {
    @Query("SELECT * FROM story")
    fun getStory() : LiveData<List<StoryList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStory(stories: List<StoryList>)

    @Query("DELETE FROM story")
    fun deleteAllStory()
}