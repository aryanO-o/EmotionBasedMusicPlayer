package com.example.emotionbasedmusic.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.network.API
import kotlinx.coroutines.launch

class MusicViewModel: ViewModel() {
    private var bitmap: Bitmap? = null

    val musicData = MutableLiveData<List<Music>>()

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

     fun getSongs(mood: String) {
        musicData.value = mutableListOf()
        viewModelScope.launch {
            when(mood) {
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

        }
    }
}

