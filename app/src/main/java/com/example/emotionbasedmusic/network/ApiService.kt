package com.example.emotionbasedmusic.network

import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.helper.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET


private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi)).baseUrl(
    Constants.BASE_URL).build()

interface ApiService {

    @GET("happy_songs_data")
    suspend fun getHappySongs(): List<Music>

    @GET("sad_songs_data")
    suspend fun getSadSongs(): List<Music>

    @GET("neutral_songs_data")
    suspend fun getNeutralSongs(): List<Music>

}


object API {
    val retrofitService : ApiService by lazy { retrofit.create(ApiService::class.java) }
}
