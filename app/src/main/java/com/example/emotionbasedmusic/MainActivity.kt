package com.example.emotionbasedmusic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.sign_up_fragment)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.login_via_mobile_fragment)
    }
}