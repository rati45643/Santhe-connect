package com.example.santheconnect.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class UploadResponse(
    val message: String,
    val imageUrl: String,
    val filename: String
)

interface FileUploadService {
    @Multipart
    @POST("/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): UploadResponse

    companion object {
        // 10.0.2.2 is the special IP for Android Emulator to access host machine localhost
        private const val BASE_URL = "http://10.0.2.2:3000/"

        fun create(): FileUploadService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FileUploadService::class.java)
        }
    }
}
