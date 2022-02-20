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
import com.example.emotionbasedmusic.eventBus.MessageEvent
import com.example.emotionbasedmusic.eventBus.PositionEvent
import com.example.emotionbasedmusic.eventBus.SongEvent
import com.example.emotionbasedmusic.fragments.MusicFragment
import com.example.emotionbasedmusic.helper.*
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable
import java.util.concurrent.TimeUnit

class MusicService : Service(), MediaPlayer.OnPreparedListener, Serializable, Runnable {

    private var urlList: MutableList<String> = mutableListOf()
    private var pagerPosition: Int? = null
    private lateinit var song: Music
    private var notification: Notification? = null
    private var notificationManager: NotificationManager? = null
    private var helper: Helper? = null
    private var index: Int? = -1
    private var handler = Handler(Looper.myLooper()!!)
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
    private var songsList: MutableLiveData<MutableList<Music>> = MutableLiveData<MutableList<Music>>()
    private var isPaused = false
    private var isLooping = false
    private var isRecyclerViewUp = true
    private var songRunnable: Runnable? = null
    private var songHandler: Handler? = Handler(Looper.myLooper()!!)
    private var likedSongsList = mutableListOf<Music>()
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
        resetMediaPlayer(true)
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
            getString(R.string.remove_from_liked) -> {
                val songEvent = event as SongEvent
                removeFromLiked(songEvent.getMusic())
            }
            getString(R.string.add_to_liked) -> {
                val songEvent = event as SongEvent
                addToLiked(songEvent.getMusic())
            }
        }
    }

    private fun addToLiked(music: Music) {
        this.likedSongsList.add(music)
    }

    private fun removeFromLiked(music: Music) {
        this.likedSongsList.remove(music)
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
    private fun setUpMediaPlayer() {
        initSome()
        mediaPlayer.apply {
            setDataSource(song.songUrl)
            setOnPreparedListener(this@MusicService)
            prepareAsync()
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        MusicFragment.binding.apply {
            pIndicator.makeVisible()
            sBar.makeInvisible()
            btnLike.makeGone()
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress = 0
            ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon_new))
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            btnPlay.isEnabled = false
            btnPlay.isClickable = false
        }
    }

    fun setSongsList(songsList: List<Music>) {
        this.songsList.value = songsList as MutableList<Music>
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
        handler.postDelayed(this, 100)
        setUpNotification()
    }

    fun pauseMediaPlayer() {
        isPaused = true
        mediaPlayer.pause()
        handler.removeCallbacks(this)
        stopForeground(false)
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
        handler.removeCallbacks(this)
        handler.postDelayed(this, 100)
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
            isLooping = true
            MusicFragment.binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_button_blue))
        } else {
            MusicFragment.binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon_new))
        }
    }



    private fun checkForPaused() {
        if (isPaused) {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
        } else {
            MusicFragment.binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
        }
    }


    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        MusicFragment.binding.apply {
            btnPlay.isEnabled = true
            btnPlay.isClickable = true
        }
        mediaPlayer?.start()
        MusicFragment.binding.pIndicator.makeGone()
        MusicFragment.binding.sBar.makeVisible()
        startTime = mediaPlayer?.currentPosition
        endTime = mediaPlayer?.duration
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
        handler.postDelayed(this, 100)
    }

    fun setLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
        isLooping = looping
    }

    fun getPagerPosition(): Int? {return pagerPosition}

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initChanges() {
        songRunnable?.let { songHandler?.removeCallbacks(it) }
        EventBus.getDefault().post(SongEvent(getString(R.string.update_song), this.song))
        initView()
        resetMediaPlayer(false)
        stopForeground(true)
        setUpNotification()
        initRunnable()
        songRunnable?.let { songHandler?.postDelayed(it, 1000) }
    }

    private fun initRunnable() {
        songRunnable = Runnable {
            setUpMediaPlayer()
        }
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
                    initToFalse()
                    EventBus.getDefault().post(PositionEvent(getString(R.string.scroll_to), index))
                    setPosition(index)
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
            } else {showToast("Empty")}
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun setPosition(pagerPosition: Int) {
        this.pagerPosition = pagerPosition
    }

    fun setImageUrlList(list: List<String>) {this.urlList = list as MutableList<String>}
    fun getImageUrlList(): List<String> = urlList
    @RequiresApi(Build.VERSION_CODES.N)
    fun prevSong() {
        if (prev) {
            if (songsList.value!!.isNotEmpty()) {
                val indexOfCurrentSong = songsList.value!!.indexOf(song)
                if (indexOfCurrentSong != 0) {
                    val index = indexOfCurrentSong - 1
                    initToFalse()
                    EventBus.getDefault().post(PositionEvent(getString(R.string.scroll_to), index))
                    setPosition(index)
                } else {
                    Toast.makeText(this, getString(R.string.out_of_songs), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun resetMediaPlayer(release: Boolean) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        handler.removeCallbacks(this)
        if(release) mediaPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

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
        handler.postDelayed(this, 100)
    }

}

