package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.app.Activity
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
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.services.MusicService
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.util.concurrent.TimeUnit

class MusicFragment : Fragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener,
    ServiceConnection {

    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }
    private var song: Music? = null
    private var intent: Intent? = null
    private var notificationManager: NotificationManager? = null
    private var songsList: MutableLiveData<List<Music>> = MutableLiveData<List<Music>>()
    private var isLooping = false
    private var key: Boolean? = true
    private lateinit var service: MusicService
    private var isBounded: Boolean? = false
    private var isFromFavorite: Boolean = false
    val _likedSongs = MutableLiveData<MutableList<Music>>()

    companion object {
        lateinit var binding: FragmentMusicBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(MusicService.currentSong?.songName==model.getSong()?.songName) {
            (requireActivity() as MainActivity).key = false
        }
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
        intent = Intent(requireContext(), MusicService::class.java)
        key = (requireActivity() as MainActivity).key
        isFromFavorite = (requireActivity() as MainActivity).isFromFavorite
        notificationManager =
            requireActivity().getSystemService(NotificationManager::class.java) as NotificationManager
        binding.sBar.setOnSeekBarChangeListener(this)
        songsList.value = mutableListOf()
        _likedSongs.value = mutableListOf()
        model.musicData.observe(viewLifecycleOwner) {
            this.songsList.value = it
        }
        model._likedSongs.observe(viewLifecycleOwner) {
            this._likedSongs.value = it
        }
        checkForKey()
        binding.apply {
            btnBack.setOnClickListener(this@MusicFragment)
            btnLike.setOnClickListener(this@MusicFragment)
            btnNextSong.setOnClickListener(this@MusicFragment)
            btnPlay.setOnClickListener(this@MusicFragment)
            ibLoop.setOnClickListener(this@MusicFragment)
            btnPreviousSong.setOnClickListener(this@MusicFragment)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkForLiked() {
        if (key!!) {

        } else {
            this.song = service.getSong()
        }
        when (_likedSongs.value!!.contains(song)) {
            true -> {
                binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_outline_favorite_24))
            }
            false -> {
                binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
            }
        }
    }

    private fun checkForKey() {
        when (key!!) {
            true -> {
                stopForegroundService()
                initView()
            }
            false -> {
                checkForService()
            }
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        this.song = model.getSong()
        binding.apply {
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress = 0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song?.songName
            tvArtist.text = song?.artistName
            Picasso.get().load(song?.imgUrl).into(ivSong)
        }
        startMusicService()
    }

    private fun startMusicService() {
        intent!!.putExtra(Constants.SONG, this.song)
        intent!!.putExtra(Constants.INDEX, songsList.value?.indexOf(song))
        ContextCompat.startForegroundService(requireContext(), intent!!)
        (requireActivity() as MainActivity).isServiceRunning = true
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
        requireActivity().unbindService(this)
    }

    private fun stopForegroundService() {
        intent?.let { requireActivity().stopService(it) }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPlay -> {
                when (MusicService.mediaPlayer.isPlaying) {
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
                if (isLooping) {
                    isLooping = false
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon))
                    setLooping(isLooping)
                } else {
                    isLooping = true
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_repeat_24_b))
                    setLooping(isLooping)
                }
            }
            R.id.btnNextSong -> {
                nextSong()
            }
            R.id.btnPreviousSong -> {
                prevSong()
            }
            R.id.btnLike -> {
                checkForFavorite()
            }
            R.id.btnBack -> {
                popBackStack()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextSong() {
        service.nextSong()
        getSong()
        checkForLiked()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun prevSong() {
        service.prevSong()
        getSong()
        checkForLiked()
    }

    private fun getSong() {
        this.song = service.getSong()
    }

    private fun popBackStack() {
        (requireActivity() as MainActivity).navController.popBackStack()
    }

    private fun checkForFavorite() {
        if (key!!) {

        } else {
            this.song = service.getSong()
        }
        if (this._likedSongs.value!!.contains(song)) {
            removeFromLiked(song!!)
        } else {
            addToLiked(song!!)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun removeFromLiked(song: Music) {
        binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
        model.removedFromLikedSongs(song)
        showToast(Constants.REMOVED_LIKED_SONGS)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addToLiked(song: Music) {
        binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_outline_favorite_24))
        model.addToLikedSongs(song)
        showToast(Constants.ADDED_LIKED_SONGS)
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
        when (key!!) {
            false -> {
                this.songsList.value = this.service.getSongsList()
                checkForLiked()
                this.service.setUI()
            }
            true -> {
                checkForLiked()
                when (isFromFavorite) {
                    true -> {
                        this.service.setSongsList(this._likedSongs.value!!)
                    }
                    false -> {
                        this.service.setSongsList(this.songsList.value!!)
                    }
                }
            }
        }
    }


    override fun onServiceDisconnected(p0: ComponentName?) {
        isBounded = false
    }


}

