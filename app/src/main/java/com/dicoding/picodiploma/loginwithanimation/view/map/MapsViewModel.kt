package com.dicoding.picodiploma.loginwithanimation.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse

class MapsViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    val StoryLocation: LiveData<StoryResponse> = repository.StoryWithLocation


    fun getStoryWithLocation(token: String) {
        repository.getStoriesByMap(token)
    }
}