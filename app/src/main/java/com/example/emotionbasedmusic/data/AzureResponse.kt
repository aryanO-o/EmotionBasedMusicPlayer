package com.example.emotionbasedmusic.data


data class AzureResponse(
    var faceAttributes: Emotions
)

data class Emotions(
    var emotion: Moods
)

data class Moods(
    var anger: Double,
    var contempt: Double,
    var disgust: Double,
    var fear: Double,
    var happiness: Double,
    var neutral: Double,
    var sadness: Double,
    var surprise: Double
)