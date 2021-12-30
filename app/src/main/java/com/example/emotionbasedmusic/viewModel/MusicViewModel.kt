package com.example.emotionbasedmusic.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MusicViewModel: ViewModel() {
    private var bitmap: Bitmap? = null

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }
}

