package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class MusicFragment : Fragment(), MediaPlayer.OnPreparedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private lateinit var binding: FragmentMusicBinding
    private val model: MusicViewModel by activityViewModels()
    private lateinit var song: Music
    private lateinit var mediaPlayer: MediaPlayer
    private var startTime: Int? = 0
    private var endTime: Int? = 0
    private var start: String? = ""
    private var end: String? = ""
    private var runnable: Runnable? = null
    private val handler = Handler(Looper.myLooper()!!)
    private var songsList: MutableLiveData<List<Music>> = MutableLiveData<List<Music>>()
    private var isLooping = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    private fun setUpMediaPlayer() {
        mediaPlayer.apply {
            setDataSource(song.songUrl)
            setOnPreparedListener(this@MusicFragment)
            prepareAsync()
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
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
        setUpMediaPlayer()
    }


    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
        startTime = mediaPlayer.currentPosition
        endTime = mediaPlayer.duration
        start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong()))}"
        end = "${TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(endTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime!!.toLong()))}"
        binding.apply {
            sBar.max = endTime!!
            tvStartTime.text = start
            tvEndTime.text = end
            sBar.progress = startTime!!
        }
        handler.postDelayed(updateTime, 100)
    }

    private var updateTime = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition
            start = "${TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong())}:${TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong())-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime!!.toLong()))}"
            binding.apply {
                tvStartTime.text = start
                sBar.progress = startTime!!
            }
            this@MusicFragment.runnable = this
            handler.postDelayed(this, 100)
        }
    }

    private fun pauseMediaPlayer() {
        mediaPlayer.pause()
        runnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyResources()
    }

    private fun destroyResources() {
        mediaPlayer.pause()
        mediaPlayer.reset()
        runnable?.let { handler.removeCallbacks(it) }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.btnPlay -> {
                when(mediaPlayer.isPlaying) {
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
                    mediaPlayer.isLooping = isLooping
                }
                else {
                    isLooping = true
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_repeat_24_b))
                    mediaPlayer.isLooping = isLooping
                }
            }
            R.id.btnNextSong -> {
                if(songsList.value!!.isNotEmpty()) {
                    val indexOfCurrentSong = songsList.value!!.indexOf(song)
                    if(indexOfCurrentSong!=songsList.value!!.size-1) {
                        val index = indexOfCurrentSong+1
                        model.setSong(songsList.value!![index])
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

    private fun resumeMediaPlayer() {
        mediaPlayer.start()
        runnable?.let { handler.postDelayed(it, 100) }
    }

    override fun onProgressChanged(sb: SeekBar?, progress: Int, p2: Boolean) {

    }

    private fun seekTo(seekPosition: Int) {
        mediaPlayer.seekTo(seekPosition)
    }

    override fun onStartTrackingTouch(sb: SeekBar?) {

    }

    override fun onStopTrackingTouch(sb: SeekBar?) {
        val progress = sb?.progress
        seekTo(progress!!)
    }


}