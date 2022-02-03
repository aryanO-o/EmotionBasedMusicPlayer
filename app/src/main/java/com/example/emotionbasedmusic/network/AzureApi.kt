package com.example.emotionbasedmusic.network

import com.example.emotionbasedmusic.data.AzureResponse
import com.example.emotionbasedmusic.data.ImageBody
import com.example.emotionbasedmusic.helper.Constants
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private val client = OkHttpClient.Builder().build()
private val retrofit =
    Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
        Constants.AZURE_BASE_URL
    ).client(client)
        .build()

interface AzureApi {
    @Headers(
        "Content-Type: application/json",
        "Ocp-Apim-Subscription-Key: ${Constants.AZURE_API_KEY}"
    )
    @POST("detect?returnFaceAttributes=emotion")
    suspend fun sendImage(@Body img: ImageBody): Response<List<AzureResponse>>
}

object AZURE {
    val azureService: AzureApi by lazy { retrofit.create(AzureApi::class.java) }
}


