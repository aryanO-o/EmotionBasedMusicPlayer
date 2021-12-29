package com.example.emotionbasedmusic.dataSource

import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.model.emojiDataModel

class emojiData {

    fun loadEmoji(): List<emojiDataModel>{
        return listOf<emojiDataModel>(
            emojiDataModel(R.drawable.neutral_face, "Neutral"),
            emojiDataModel(R.drawable.happy_face, "Happy"),
            emojiDataModel(R.drawable.sad_face, "Sad"),
            emojiDataModel(R.drawable.surprised_face, "Surprised"),
            emojiDataModel(R.drawable.angry_face, "Angry"),
            emojiDataModel(R.drawable.tired_face, "Tired"),

        )
    }
}