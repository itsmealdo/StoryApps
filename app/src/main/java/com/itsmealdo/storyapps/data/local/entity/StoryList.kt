package com.itsmealdo.storyapps.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryList (
    @field:ColumnInfo(name = "id")
    @PrimaryKey
    val id: String,

    @field:ColumnInfo(name = "photoUrl")
    val photoUrl: String,

    @field:ColumnInfo(name = "createdAt")
    val createdAt: String,

    @field:ColumnInfo(name = "name")
    val name: String,

    @field:ColumnInfo(name = "description")
    val description: String,

    @field:ColumnInfo(name = "lon")
    val lon: Double,

    @field:ColumnInfo(name = "lat")
    val lat: Double
)