package com.example.emotionbasedmusic.eventBus

class MessageEvent(private val str: String) {
    fun getString() : String {
        return str
    }
}