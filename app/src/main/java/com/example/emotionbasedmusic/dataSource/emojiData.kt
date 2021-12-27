package com.example.emotionbasedmusic.dataSource

import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.model.emojiDataModel

class emojiData {

    fun loadEmoji(): List<emojiDataModel>{
        return listOf<emojiDataModel>(
            emojiDataModel(R.drawable.cool_emoji, "happy"),
            emojiDataModel(R.drawable.cool_emoji, "sad"),
            emojiDataModel(R.drawable.cool_emoji, "gratitude"),
            emojiDataModel(R.drawable.cool_emoji, "happy"),
            emojiDataModel(R.drawable.cool_emoji, "sad"),
            emojiDataModel(R.drawable.cool_emoji, "gratitude"),
            emojiDataModel(R.drawable.cool_emoji, "happy"),
            emojiDataModel(R.drawable.cool_emoji, "sad"),
            emojiDataModel(R.drawable.cool_emoji, "gratitude")

        )
    }
}