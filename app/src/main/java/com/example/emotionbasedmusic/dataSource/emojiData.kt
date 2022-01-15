package com.example.emotionbasedmusic.dataSource

import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.emojiDataModel
import com.example.emotionbasedmusic.helper.Constants

class emojiData {

    fun loadEmoji(): List<emojiDataModel>{
        return listOf(
            emojiDataModel(R.drawable.neutral_face, Constants.NEUTRAL_MOOD),
            emojiDataModel(R.drawable.happy_face, Constants.HAPPY_MOOD),
            emojiDataModel(R.drawable.sad_face, Constants.SAD_MOOD)
//            emojiDataModel(R.drawable.surprised_face, "Surprised"),
//            emojiDataModel(R.drawable.angry_face, "Angry"),
//            emojiDataModel(R.drawable.tired_face, "Tired"),
        )
    }
}