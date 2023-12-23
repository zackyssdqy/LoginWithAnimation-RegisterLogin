package com.dicoding.picodiploma.loginwithanimation.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.Paging
import com.dicoding.picodiploma.loginwithanimation.Result
import com.dicoding.picodiploma.loginwithanimation.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.response.SignupResponse
import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.response.UploadStoryResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.UnknownHostException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    fun register(name: String, email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val success = apiService.register(name, email, password)
            emit(Result.Success(success))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = Gson().fromJson(errorBody, SignupResponse::class.java)
            emit(error.message?.let { Result.Error(it) })
        }
    }

    fun login(email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val success = apiService.login(email, password)
            emit(Result.Success(success))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val error = Gson().fromJson(errorBody, LoginResponse::class.java)
            emit(error.message?.let { Result.Error(it) })
        }
    }

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 4
            ),
            pagingSourceFactory = {
                Paging(token, apiService)
            }
        ).liveData
    }

    fun uploadStory(token: String, imageFile: File, description: String) = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val successResponse = apiService.uploadStory(token, multipartBody, requestBody)
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, UploadStoryResponse::class.java)
            emit(Result.Error(errorResponse.message))
        }

    }

    private val _StoryWithLocation = MutableLiveData<StoryResponse>()
    val StoryWithLocation: LiveData<StoryResponse> = _StoryWithLocation
    fun getStoriesByMap(token: String) = liveData {
        emit(Result.Loading)

        try {
            val storyResponse: StoryResponse = apiService.getStoriesWithLocation("Bearer $token")

            emit(Result.Success(storyResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.message()

            when (val code = e.code()) {
                in 300..399 -> {
                    emit(Result.Error("Need To Reconfigure. Please Contact Administrator"))
                }

                in 400..499 -> {
                    emit(Result.Error("Request Error. code $code $errorBody"))
                }

                in 500..599 -> {
                    emit(Result.Error("Server Error"))
                }
            }
        } catch (e: UnknownHostException) {
            emit(Result.Error("Error... Please check your connection"))
        } catch (e: Exception) {
            emit(Result.Error("Unknown Error"))
        }

    }


    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}