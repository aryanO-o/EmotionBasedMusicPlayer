package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.adapter.MusicAdapter
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentFavoritesBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory


class FavoritesFragment : Fragment(), MusicAdapter.IFavorite {
    private var binding: FragmentFavoritesBinding? = null
    private lateinit var adapter: MusicAdapter
    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritesBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar()
        setUpRecyclerView()
    }

    private fun initToolbar() {
        binding?.apply {
            tbFavorite.tbCommon.menu.clear()
            tbFavorite.tbCommon.setTitleTextColor(resources.getColor(R.color.white))
            tbFavorite.tbCommon.title = "Favorites"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpRecyclerView() {
        adapter = MusicAdapter(null, requireContext(), false, 1, this)
        binding?.apply {
            rvFavorites.adapter = adapter
            rvFavorites.layoutManager = GridLayoutManager(requireContext(), 1)
        }
        model._likedSongs.observe(viewLifecycleOwner) {
            checkForSongs(it)
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun checkForSongs(it: MutableList<Music>?) {
        when (it!!.isEmpty()) {
            true -> {
                binding?.clEmpty?.makeVisible()
            }
            false -> {
                binding?.clEmpty?.makeGone()
            }
        }
    }

    override fun onRemoveClick(song: Music) {
        model.removedFromLikedSongs(song)
        Toast.makeText(requireContext(), Constants.REMOVED_LIKED_SONGS, Toast.LENGTH_SHORT).show()
    }

    override fun onItemSongClick(song: Music) {
        (requireActivity() as MainActivity).key = true
        (requireActivity() as MainActivity).isFromFavorite = true
        model.setSong(song)
        findNavController().navigate(R.id.action_favoritesFragment_to_musicFragment)
    }
}