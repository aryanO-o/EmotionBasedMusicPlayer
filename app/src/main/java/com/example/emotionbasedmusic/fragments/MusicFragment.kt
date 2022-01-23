package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.services.MusicService
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class MusicFragment : Fragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener, ServiceConnection {

    private val model: MusicViewModel by activityViewModels()
    private var song: Music? = null
    private lateinit var mediaPlayer: MediaPlayer
    private var startTime: Int? = 0
    private var endTime: Int? = 0
    private var start: String? = ""
    private var end: String? = ""
    private var runnable: Runnable? = null
    private var intent: Intent? = null
    private lateinit var connection: ServiceConnection
    private var notificationManager: NotificationManager? = null
    private val handler = Handler(Looper.myLooper()!!)
    private var songsList: MutableLiveData<List<Music>> = MutableLiveData<List<Music>>()
    private var isLooping = false
    private var key: Boolean? = true
    private lateinit var service: MusicService
    private var isBounded: Boolean? = false
    companion object {
        lateinit var binding: FragmentMusicBinding
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        key = model.key
        notificationManager = requireActivity().getSystemService(NotificationManager::class.java) as NotificationManager
        binding.sBar.setOnSeekBarChangeListener(this)
        songsList.value = mutableListOf()
        model.musicData.observe(viewLifecycleOwner) {
            this.songsList.value = it
        }
        initMediaPlayer()
        initView()
        binding.apply {
            btnNextSong.setOnClickListener(this@MusicFragment)
            btnPlay.setOnClickListener(this@MusicFragment)
            ibLoop.setOnClickListener(this@MusicFragment)
            btnPreviousSong.setOnClickListener(this@MusicFragment)
        }
    }

    private fun checkForService() {
        bindToService()
    }

    private fun bindToService() {
        Intent(requireContext(), MusicService::class.java).also {
            requireActivity().bindService(it, this, Context.BIND_AUTO_CREATE)
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



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        this.song = model.getSong()
        binding.apply {
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress=0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song?.songName
            tvArtist.text = song?.artistName
            Picasso.get().load(song?.imgUrl).into(ivSong)
        }
        startMusicService()
    }


    private fun startMusicService() {
        val intent = Intent(requireContext(), MusicService::class.java)
        this.intent = intent
        intent.putExtra("song", this.song)
        ContextCompat.startForegroundService(requireContext(), intent)
        bindToService()
    }

    private fun pauseMediaPlayer() {
        service.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyResources()
    }

    private fun destroyResources() {
        service.resetMediaPlayer()
        requireActivity().unbindService(this)
        stopForegroundService()
    }

    private fun stopForegroundService() {
        intent?.let { requireActivity().stopService(it) }
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.btnPlay -> {
                when(MusicService.mediaPlayer.isPlaying) {
                    true -> {
                        pauseMediaPlayer()
                        binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
                    }
                    false -> {
                        resumeMediaPlayer()
                        binding.btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
                    }
                }
            }
            R.id.ibLoop -> {
                if(isLooping) {
                    isLooping = false
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon))
                    setLooping(isLooping)
                }
                else {
                    isLooping = true
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_repeat_24_b))
                    setLooping(isLooping)
                }
            }
            R.id.btnNextSong -> {
                if(songsList.value!!.isNotEmpty()) {
                    val indexOfCurrentSong = songsList.value!!.indexOf(song)
                    if(indexOfCurrentSong!=songsList.value!!.size-1) {
                        val index = indexOfCurrentSong+1
                        model.setSong(songsList.value!![index])
                        service.resetMediaPlayer()
                        destroyResources()
                        initView()
                    }
                    else {
                        Toast.makeText(requireContext(), getString(R.string.out_of_songs), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.btnPreviousSong -> {
                if(songsList.value!!.isNotEmpty()) {
                    val indexOfCurrentSong = songsList.value!!.indexOf(song)
                    if(indexOfCurrentSong!=0) {
                        val index = indexOfCurrentSong-1
                        model.setSong(songsList.value!![index])
                        service.resetMediaPlayer()
                        destroyResources()
                        initView()
                    }
                    else {
                        Toast.makeText(requireContext(), getString(R.string.out_of_songs), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setLooping(looping: Boolean) {
        service.setLooping(looping)
    }

    private fun resumeMediaPlayer() {
        service.resumeMediaPlayer()
    }

    override fun onProgressChanged(sb: SeekBar?, progress: Int, p2: Boolean) {

    }

    private fun seekTo(seekPosition: Int) {
        service.seekTo(seekPosition)
    }

    override fun onStartTrackingTouch(sb: SeekBar?) {

    }

    override fun onStopTrackingTouch(sb: SeekBar?) {
        val progress = sb?.progress
        seekTo(progress!!)
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.LocalBinder
        this.service = binder.getService()
        isBounded = true
        this.service.setUI()
    }


    override fun onServiceDisconnected(p0: ComponentName?) {
        isBounded = false
    }


}