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
import com.example.emotionbasedmusic.Utils.createNotificationChannel
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.eventBus.MessageEvent
import com.example.emotionbasedmusic.eventBus.PositionEvent
import com.example.emotionbasedmusic.fragments.MusicFragment
import com.example.emotionbasedmusic.helper.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable
import java.util.concurrent.TimeUnit

class MusicService : Service(), MediaPlayer.OnPreparedListener, Serializable {

    private var urlList: List<String> = listOf()
    private var pagerPosition: Int? = null
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
    private var isLooping = false
    private var isRecyclerViewUp = true
    var next = false
    var prev = false
    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    companion object {
        lateinit var mediaPlayer: MediaPlayer
        var currentSong: Music? = null
    }

    override fun onCreate() {
        super.onCreate()
        if(!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        resetMediaPlayer()
        stopForeground(true)
        EventBus.getDefault().unregister(this)
    }


    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        when (event?.getString()) {
            getString(R.string.activity_destroyed) -> {
                isRecyclerViewUp = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.song = intent?.extras?.get(Constants.SONG) as Music
        currentSong = this.song
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
            helper = createNotification(this@MusicService, song)
        }
        notification = helper?.notification
        val notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        this@MusicService.notificationManager = notificationManager
        notification?.let { startForeground(Constants.ID, it) }
        notification?.let {
            Picasso.get().load(song.imgUrl)
                .into(helper?.remoteViews!!, R.id.notification_album_image, Constants.ID, it)
            initToTrue()
        }
    }

    private fun initToTrue() {
        next = true
        prev = true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        MusicFragment.binding.apply {
            pIndicator.makeVisible()
            sBar.makeInvisible()
            btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress = 0
            ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon))
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
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



    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeMediaPlayer() {
        isPaused = false
        mediaPlayer.start()
        runnable?.let { handler.postDelayed(it, 100) }
        setUpNotification()
    }

    fun pauseMediaPlayer() {
        isPaused = true
        mediaPlayer.pause()
        runnable?.let { handler.removeCallbacks(it) }
        stopForeground(false)
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
            sBar.progress = 0
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
        }
        checkForPaused()
        checkForLooping()
    }

    private fun checkForLooping() {
        if (isLooping) {
            MusicFragment.binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_repeat_24_b))
        } else {
            MusicFragment.binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon))
        }
    }



    private fun checkForPaused() {
        if (isPaused) {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
        } else {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
        }
    }

    private var updateTime = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition
            last =
                TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())
                )
            if (last in 0..9) {
                lastString = "0$last"
                start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:" + lastString
            } else {
                start =
                    "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:" + last.toString()
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
        start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:${
            TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())
            )
        }"
        endL = TimeUnit.MILLISECONDS.toSeconds(endTime!!.toLong()) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())
        )
        if (endL in 0..9) {
            endString = "0$endL"
            end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:" + endString
        } else {
            end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:" + endL.toString()
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
        isLooping = looping
    }

    fun getPagerPosition(): Int? {return pagerPosition}

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initChanges() {
        initToFalse()
        resetMediaPlayer()
        initView()
        stopForeground(true)
        setUpNotification()
        setUpMediaPlayer()
    }

    private fun initToFalse() {
        next = false
        prev = false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextSong() {
        if (next) {
            if (songsList.value!!.isNotEmpty()) {
                val indexOfCurrentSong = songsList.value!!.indexOf(song)
                if (indexOfCurrentSong != songsList.value!!.size - 1) {
                    val index = indexOfCurrentSong + 1
                    this.song = songsList.value!![index]
                    EventBus.getDefault().post(PositionEvent(getString(R.string.scroll_to), index))
                    setPosition(index)
                    initChanges()
                } else {
                    Toast.makeText(this, getString(R.string.out_of_songs), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, getString(R.string.songs_list_empty), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    fun setTo(position: Int) {
        if(songsList.value!!.isNotEmpty()) {
            if(position!=songsList.value?.size){
                this.song = songsList.value!![position]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    initChanges()
                }
            }
        }
    }

    fun setPosition(pagerPosition: Int) {
        this.pagerPosition = pagerPosition
    }

    fun setImageUrlList(list: List<String>) {this.urlList = list}
    fun getImageUrlList(): List<String> = urlList
    @RequiresApi(Build.VERSION_CODES.N)
    fun prevSong() {
        if (prev) {
            if (songsList.value!!.isNotEmpty()) {
                val indexOfCurrentSong = songsList.value!!.indexOf(song)
                if (indexOfCurrentSong != 0) {
                    val index = indexOfCurrentSong - 1
                    this.song = songsList.value!![index]
                    EventBus.getDefault().post(PositionEvent(getString(R.string.scroll_to), index))
                    setPosition(index)
                    initChanges()
                } else {
                    Toast.makeText(this, getString(R.string.out_of_songs), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun resetMediaPlayer() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        runnable?.let { handler.removeCallbacks(it) }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }



}

