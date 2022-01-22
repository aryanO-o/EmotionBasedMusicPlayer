package com.example.emotionbasedmusic.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.fragments.ResultSongsFragment
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.network.API
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class MusicViewModel: ViewModel() {
    private var bitmap: Bitmap? = null
    private lateinit var mood: String
    val musicData = MutableLiveData<List<Music>>()
    private lateinit var song: Music
    fun setSong(song: Music) {
        this.song = song
    }

    fun getSong(): Music {
        return song
    }
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun getMood(): String {
        return mood
    }

     fun getSongs(mood: String) {
         this.mood = mood
         musicData.value = mutableListOf()
         viewModelScope.launch {
             try {
                 when (mood) {
                     Constants.HAPPY_MOOD -> {
                         musicData.value = API.retrofitService.getHappySongs()
                     }
                     Constants.SAD_MOOD -> {
                         musicData.value = API.retrofitService.getSadSongs()
                     }
                     Constants.NEUTRAL_MOOD -> {
                         musicData.value = API.retrofitService.getNeutralSongs()
                     }
                 }
             } catch (e: Exception) {
                 ResultSongsFragment.binding.neResultSongs.ne.makeVisible()
             }
         }
    }
}

