package com.example.emotionbasedmusic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.FragmentFaceProceedOrRetakeBinding
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.viewModel.MusicViewModel


class FaceProceedOrRetakeFragment : Fragment() {
    private lateinit var binding: FragmentFaceProceedOrRetakeBinding
    private val model: MusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaceProceedOrRetakeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}