package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.squareup.picasso.Picasso

class MusicFragment : Fragment(), MediaPlayer.OnPreparedListener, View.OnClickListener {
    private lateinit var binding: FragmentMusicBinding
    private val model: MusicViewModel by activityViewModels()
    private lateinit var song: Music
    private lateinit var mediaPlayer: MediaPlayer
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initMediaPlayer()
        initView()
        binding.apply {
            btnPlay.setOnClickListener(this@MusicFragment)
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

    private fun initView() {
        this.song = model.getSong()
        binding.apply {
            tvSongName.text = song.songName
            tvArtist.text = song.artistName
            Picasso.get().load(song.imgUrl).into(ivSong)
        }
        setUpMediaPlayer()
    }


    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
    }

    private fun pauseMediaPlayer() {
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.pause()
        mediaPlayer.reset()
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
        }
    }

    private fun resumeMediaPlayer() {
        mediaPlayer.start()
    }

}