package com.example.emotionbasedmusic.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import androidx.annotation.RequiresApi
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.Utils.createNotification
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.fragments.MusicFragment
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.Helper
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class MusicService : Service(), MediaPlayer.OnPreparedListener {

    private lateinit var song: Music
    private var notification: Notification? = null
    private var notificationManager: NotificationManager? = null
    private var helper: Helper? = null
    private val handler = Handler(Looper.myLooper()!!)
    private var startTime: Int? = 0
    private var endTime: Int? = 0
    private var start: String? = ""
    private var end: String? = ""
    private var runnable: Runnable? = null
    private val binder = LocalBinder()
    private var last: Long = 0
    private var lastString = ""
    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    companion object {
         lateinit var mediaPlayer: MediaPlayer
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.song = intent?.extras?.get("song") as Music
        initMediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            helper = createNotification(this, song)
        }
        notification = helper?.notification
        val notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        this.notificationManager = notificationManager
        notification?.let { startForeground(Constants.ID, it) }
        notification?.let {
            Picasso.get().load(song.imgUrl)
                .into(helper?.remoteViews!!, R.id.notification_album_image, Constants.ID, it)
        }
        setUpMediaPlayer()
        return START_STICKY
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        MusicFragment.binding.apply {
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress=0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
    }

    override fun onDestroy() {
        stopForeground(true)
    }


    fun resumeMediaPlayer() {
        mediaPlayer.start()
        runnable?.let { handler.postDelayed(it, 100) }
    }

    fun pauseMediaPlayer() {
        mediaPlayer.pause()
        runnable?.let { handler.removeCallbacks(it) }
    }

    private fun setUpMediaPlayer() {
        mediaPlayer.apply {
            setDataSource(song.songUrl)
            setOnPreparedListener(this@MusicService)
            prepareAsync()
        }
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    fun seekTo(seekPosition: Int) {
        mediaPlayer.seekTo(seekPosition)
    }

    fun setUI() {
        initViews()
        runnable?.let { handler.removeCallbacks(it) }
        handler.postDelayed(updateTime, 100)
    }

    private fun initViews() {
        MusicFragment.binding.apply {
            sBar.max = endTime!!
            tvEndTime.text = end
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
    }

    private var updateTime = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition
            last = TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong()))
            if(last in 0..9) {
                lastString = "0$last"
                start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:"+lastString
            }
            else {
                start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:"+last.toString()
            }
            MusicFragment.binding.apply {
                tvStartTime.text = start
                sBar.progress = startTime!!
            }
            this@MusicService.runnable = this
            handler.postDelayed(this, 100)
        }
    }
    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
        startTime = mediaPlayer.currentPosition
        endTime = mediaPlayer.duration
        start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong()))}"
        end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(endTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong()))}"
        MusicFragment.binding.apply {
            sBar.max = endTime!!
            tvStartTime.text = start
            tvEndTime.text = end
            sBar.progress = startTime!!
        }
        handler.postDelayed(updateTime, 100)
    }

    fun setLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
    }


    fun resetMediaPlayer() {
        mediaPlayer.pause()
        mediaPlayer.reset()
        runnable?.let { handler.removeCallbacks(it) }
    }

}