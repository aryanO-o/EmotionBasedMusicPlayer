package com.example.emotionbasedmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.MusicListItemGridBinding
import com.example.emotionbasedmusic.databinding.MusicListItemViewBinding
import com.example.emotionbasedmusic.fragments.ResultSongsFragment
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.squareup.picasso.Picasso

class MusicAdapter: ListAdapter<Music, MusicAdapter.MusicViewHolder>(DiffCallback) {

    class MusicViewHolder(binding: MusicListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
         val songName = binding.songName
         val artistName = binding.artistName
         val img = binding.songImgView
    }


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Music>() {
            override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
                return  oldItem.songName == newItem.songName
            }

            override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
                return oldItem.imgUrl == newItem.imgUrl
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val adapterLayout = MusicListItemViewBinding.inflate(LayoutInflater.from(parent.context))
        return MusicViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            ResultSongsFragment.binding.pfDetect.pFrame.makeGone()
            ResultSongsFragment.binding.pfDetect.progressBarLayout.progressBar.makeGone()
            ResultSongsFragment.binding.cl1.makeGone()
            ResultSongsFragment.binding.cl2.makeVisible()
            val song = getItem(position)
            holder.songName.text = song.songName
            holder.artistName.text = song.artistName

            Picasso.get().load(song.imgUrl).into(holder.img)
    }
}