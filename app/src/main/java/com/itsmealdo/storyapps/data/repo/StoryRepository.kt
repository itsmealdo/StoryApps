package com.itsmealdo.storyapps.data.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.itsmealdo.storyapps.data.local.entity.StoryList
import com.itsmealdo.storyapps.data.local.room.StoryDao
import com.itsmealdo.storyapps.data.remote.model.AddStoryResponse
import com.itsmealdo.storyapps.data.remote.model.Result
import com.itsmealdo.storyapps.data.remote.model.StoryResponse
import com.itsmealdo.storyapps.data.remote.network.ApiService
import com.itsmealdo.storyapps.utils.storyExecutor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryRepository private constructor(
    private val storyDao: StoryDao,
    private val apiService: ApiService,
    private val storyExecutor: storyExecutor
) {
    private val getUserStoryResult = MediatorLiveData<Result<List<StoryList>>>()
    private val postUserStoryResult = MediatorLiveData<Result<List<AddStoryResponse>>>()

    fun postUserStory(token: String, imageFile: File, description: String): MediatorLiveData<Result<List<AddStoryResponse>>> {
        postUserStoryResult.postValue(Result.Loading)

        val textMediaType = "text/plain".toMediaType()
        val imageMediaType = "image/jpeg".toMediaTypeOrNull()

        val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            imageFile.asRequestBody(imageMediaType)
        )

        val descriptionBody = description.toRequestBody(textMediaType)

        val postResult = MutableLiveData<Result<AddStoryResponse>>()

        val postClient = apiService.addStory(token, imagePart, descriptionBody)
        postClient.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(
                call: Call<AddStoryResponse>,
                response: Response<AddStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        postResult.postValue(Result.Success(responseBody))
                    } else {
                        postResult.postValue(Result.Error(POST_MESSAGE_ERROR))
                        Log.e(TAG, "Failed: story post response body is null")
                    }
                } else {
                    postResult.postValue(Result.Error(POST_MESSAGE_ERROR))
                    Log.e(TAG, "Failed: sorry, story post response unsuccessful - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                postResult.postValue(Result.Error(POST_MESSAGE_ERROR))
                Log.e(TAG, "Failed: story post response failure - ${t.message}")
            }
        })
        return postUserStoryResult
    }

    fun getUserStory(token: String): LiveData<Result<List<StoryList>>> {
        getUserStoryResult.value = Result.Loading
        val getStoriesClient = apiService.getStoryData(token)
        getStoriesClient.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                if (response.isSuccessful) {
                    val storyResponses = response.body()?.listStory
                    val storyEntityList = ArrayList<StoryList>()

                    storyExecutor.diskIO.execute {
                        storyResponses?.forEach {
                            val storyEntity = StoryList(
                                it.id,
                                it.photoUrl,
                                it.createdAt,
                                it.name,
                                it.description,
                                it.lon,
                                it.lat
                            )

                            storyEntityList.add(storyEntity)
                        }

                        storyDao.deleteAllStory()
                        storyDao.insertStory(storyEntityList)
                    }
                } else {
                    Log.e(TAG, "Failed: Get stories response unsuccessful - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                getUserStoryResult.value = Result.Error(t.message.toString())
                Log.e(TAG, "Failed: Get stories response some failure - ${t.message.toString()}")
            }
        })

        val localStoriesData = storyDao.getStory()
        getUserStoryResult.addSource(localStoriesData) {
            getUserStoryResult.value = Result.Success(it)
        }

        return getUserStoryResult
    }


    companion object {
        private val TAG = StoryRepository::class.java.simpleName
        private const val POST_MESSAGE_ERROR = "Story was not posted, please try again later."

        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            storyDao: StoryDao,
            apiService: ApiService,
            storyExecutor: storyExecutor
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(storyDao, apiService, storyExecutor)
            }.also { instance = it }
    }
}