package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionbasedmusic.adapter.MusicAdapter
import com.example.emotionbasedmusic.databinding.FragmentsResultSongsBinding
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.viewModel.MusicViewModel

class ResultSongsFragment: Fragment() {


    private lateinit var adapter: MusicAdapter
    private val model: MusicViewModel by activityViewModels()

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
    }

    private fun initView() {
        binding.cl1.makeVisible()
        binding.pfDetect.progressBarLayout.progressBar.makeVisible()
        binding.pfDetect.pFrame.makeVisible()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpRecyclerView() {
        adapter = MusicAdapter()
        binding.rvSongResult.adapter = adapter
        binding.rvSongResult.layoutManager = GridLayoutManager(requireContext(), 1)
        model.musicData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }
    }

}