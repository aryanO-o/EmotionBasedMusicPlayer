package com.example.emotionbasedmusic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.adapter.emojiAdapter
import com.example.emotionbasedmusic.dataSource.emojiData
import com.example.emotionbasedmusic.databinding.FragmentFaceProceedOrRetakeBinding
import com.example.emotionbasedmusic.databinding.FragmentMoodRecognitionBinding
import com.example.emotionbasedmusic.viewModel.MusicViewModel


class MoodRecognitionFragment : Fragment() {
    private lateinit var binding: FragmentMoodRecognitionBinding
    private val model: MusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoodRecognitionBinding.inflate(inflater)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emojiDataSet = emojiData().loadEmoji();
        val emojiRecyclerView = view?.findViewById<RecyclerView>(R.id.emoji_recycler_view)
        emojiRecyclerView?.adapter = emojiAdapter(this.requireContext(), emojiDataSet);
    }
}