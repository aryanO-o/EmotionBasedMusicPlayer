package com.example.emotionbasedmusic.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import com.example.emotionbasedmusic.Utils.createNotification
import com.example.emotionbasedmusic.data.Music

class MusicService: Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var song: Music
     private lateinit var notification: Notification
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.song = intent?.extras?.get("song") as Music
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification = createNotification(this)
        }
        startAsForeground()
        return START_STICKY
    }

    private fun startAsForeground() {

    }


}