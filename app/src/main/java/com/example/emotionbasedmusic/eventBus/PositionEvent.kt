package com.example.emotionbasedmusic.eventBus

data class PositionEvent(
    val str: String, val position: Int
): MessageEvent(str) {
    fun getIndex(): Int = position
}