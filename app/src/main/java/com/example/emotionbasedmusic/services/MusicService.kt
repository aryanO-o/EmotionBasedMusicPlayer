package com.example.emotionbasedmusic.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.Utils.createNotification
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.fragments.MusicFragment
import com.example.emotionbasedmusic.helper.*
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.util.concurrent.TimeUnit

class MusicService : Service(), MediaPlayer.OnPreparedListener, Serializable {

    private lateinit var song: Music
    private var notification: Notification? = null
    private var notificationManager: NotificationManager? = null
    private var helper: Helper? = null
    private var index: Int? = -1
    private val handler = Handler(Looper.myLooper()!!)
    private var startTime: Int? = 0
    private var endTime: Int? = 0
    private var start: String? = ""
    private var end: String? = ""
    private var runnable: Runnable? = null
    private val binder = LocalBinder()
    private var last: Long = 0
    private var lastString = ""
    private var endL: Long = 0
    private var endString = ""
    private var songsList: MutableLiveData<List<Music>> = MutableLiveData<List<Music>>()
    private var isPaused = false

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    companion object {
         lateinit var mediaPlayer: MediaPlayer
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.song = intent?.extras?.get(Constants.SONG) as Music
        this.index = intent.extras?.getInt(Constants.INDEX)
        songsList.value = mutableListOf()
        initMediaPlayer()
        setUpNotification()
        setUpMediaPlayer()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpNotification() {
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        MusicFragment.binding.apply {
            pIndicator.makeVisible()
            sBar.makeInvisible()
            btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress=0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
    }

    fun setSongsList(songsList: List<Music>) {
        this.songsList.value = songsList
    }

    fun getSongsList(): List<Music> {
        return this.songsList.value!!
    }

    fun getSong(): Music {
        return this.song
    }

    override fun onDestroy() {
        resetMediaPlayer()
        stopForeground(true)
    }


    fun resumeMediaPlayer() {
        isPaused = false
        mediaPlayer.start()
        runnable?.let { handler.postDelayed(it, 100) }
    }

    fun pauseMediaPlayer() {
        isPaused = true
        mediaPlayer.pause()
        runnable?.let { handler.removeCallbacks(it) }
    }

    private fun setUpMediaPlayer() {
        initSome()
        mediaPlayer.apply {
            setDataSource(song.songUrl)
            setOnPreparedListener(this@MusicService)
            prepareAsync()
        }
    }

    private fun initSome() {
        MusicFragment.binding.apply {
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress=0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViews() {
        MusicFragment.binding.apply {
            MusicFragment.binding.pIndicator.makeGone()
            MusicFragment.binding.sBar.makeVisible()
            sBar.max = endTime!!
            tvEndTime.text = end
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
        if(isPaused) {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
        }
        else {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
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
        MusicFragment.binding.pIndicator.makeGone()
        MusicFragment.binding.sBar.makeVisible()
        startTime = mediaPlayer.currentPosition
        endTime = mediaPlayer.duration
        start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong()))}"
        endL = TimeUnit.MILLISECONDS.toSeconds(endTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong()))
        if(endL in 0..9) {
            endString = "0$endL"
            end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:"+endString
        }
        else {
            end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:"+endL.toString()
        }

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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initChanges() {
        resetMediaPlayer()
        initView()
        setUpNotification()
        setUpMediaPlayer()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextSong() {
        if(songsList.value!!.isNotEmpty()) {
            val indexOfCurrentSong = songsList.value!!.indexOf(song)
            if(indexOfCurrentSong!=songsList.value!!.size-1) {
                val index = indexOfCurrentSong + 1
                this.song = songsList.value!![index]
                initChanges()

            }
            else {
                Toast.makeText(this, getString(R.string.out_of_songs), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, getString(R.string.songs_list_empty), Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun prevSong() {
        if(songsList.value!!.isNotEmpty()) {
            val indexOfCurrentSong = songsList.value!!.indexOf(song)
            if(indexOfCurrentSong!=0) {
                val index = indexOfCurrentSong-1
                this.song = songsList.value!![index]
                initChanges()
            }
            else {
                Toast.makeText(this, getString(R.string.out_of_songs), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetMediaPlayer() {
        mediaPlayer.pause()
        mediaPlayer.reset()
        runnable?.let { handler.removeCallbacks(it) }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

}

