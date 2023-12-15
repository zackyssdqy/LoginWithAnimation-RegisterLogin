package com.dicoding.picodiploma.loginwithanimation.response

import com.google.gson.annotations.SerializedName

data class UploadStoryResponse(
    @field:SerializedName("error")
    val error: Boolean,
    @field:SerializedName("message")
    val message: String
)
