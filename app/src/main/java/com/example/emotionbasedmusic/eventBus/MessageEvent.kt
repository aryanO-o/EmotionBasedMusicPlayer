package com.example.emotionbasedmusic.eventBus

open class MessageEvent(private val str: String) {
    fun getString() : String {
        return str
    }
}