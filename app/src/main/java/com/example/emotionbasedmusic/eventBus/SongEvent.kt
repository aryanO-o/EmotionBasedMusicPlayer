package com.example.emotionbasedmusic.eventBus

import com.example.emotionbasedmusic.data.Music

data class SongEvent(
    val str: String, private val song: Music
): MessageEvent(str) {
    fun getMusic(): Music = song
}