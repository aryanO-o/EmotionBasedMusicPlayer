package com.example.emotionbasedmusic.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.Color.RED
import android.hardware.camera2.params.RggbChannelVector.RED
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.helper.Constants


@RequiresApi(Build.VERSION_CODES.M)
fun createNotification(context: Context): Notification {
    val requestCode = (1..5000).random()
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    setUpNotificationChannel(context)

    val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.music_notes)
        .build()
    
    return notification
}

fun setUpNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context)
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannel(context: Context) {
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    channel.enableLights(true)
    channel.lightColor = Color.RED
    notificationManager.createNotificationChannel(channel)
}
