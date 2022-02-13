package com.example.emotionbasedmusic.container

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.FragmentBaseBinding
import com.example.emotionbasedmusic.eventBus.MessageEvent
import com.example.emotionbasedmusic.helper.Constants
import org.greenrobot.eventbus.EventBus


open class BaseFragment : Fragment() {

    private var binding: FragmentBaseBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().navigate(R.id.checkFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}