package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.adapter.MusicAdapter
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentsResultSongsBinding
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory

class ResultSongsFragment : Fragment(), MusicAdapter.IPost, MediaPlayer.OnPreparedListener,
    View.OnClickListener {

    private lateinit var adapter: MusicAdapter
    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }
    private lateinit var mediaPlayer: MediaPlayer
    private var song: Music? = null
    private var index: Int = -1

    companion object {
        lateinit var binding: FragmentsResultSongsBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentsResultSongsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        initView()
        binding.neResultSongs.btnRetry.setOnClickListener(this)
    }

    private fun initView() {
        binding.cl1.makeVisible()
        binding.pfDetect.progressBarLayout.progressBar.makeVisible()
        binding.pfDetect.pFrame.makeVisible()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpRecyclerView() {
        adapter = MusicAdapter(this, requireContext(), true, 0, null)
        binding.rvSongResult.adapter = adapter
        binding.rvSongResult.layoutManager = GridLayoutManager(requireContext(), 1)
        model.musicData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPlay(song: Music, position: Int) {
        this.index = position
        this.song = song
        resetMediaPlayer()
        mediaPlayer.apply {
            setDataSource(song.songUrl)
            setOnPreparedListener(this@ResultSongsFragment)
            prepareAsync()
        }
    }

    override fun onPauseMusic() {
        mediaPlayer.pause()
        mediaPlayer.stop()
    }

    private fun resetMediaPlayer() {
        mediaPlayer.reset()
    }

    override fun onItemSongClick(song: Music) {
        (requireActivity() as MainActivity).key = true
        (requireActivity() as MainActivity).isFromFavorite = false
        model.setSong(song)
        findNavController().navigate(R.id.action_resultSongsFragment_to_musicFragment)
    }

    private fun resetView() {
        resetMediaPlayer()
        this.song?.playing = false
        adapter.notifyItemChanged(index)
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer!!.start()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRetry -> {
                binding.neResultSongs.ne.makeGone()
                model.getSongs(model.getMood())
            }
        }
    }

}