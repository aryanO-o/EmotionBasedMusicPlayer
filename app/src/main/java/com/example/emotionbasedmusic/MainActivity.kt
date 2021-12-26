package com.example.emotionbasedmusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_face_proceed_or_retake)

        //adding adapter on emoji list view of mood recognition fragment
//        val emojiDataSet = emojiData().loadEmoji();
//        val emojiRecyclerView = findViewById<RecyclerView>(R.id.emoji_recycler_view);
//        emojiRecyclerView.adapter = emojiAdapter(this, emojiDataSet);


    }
}