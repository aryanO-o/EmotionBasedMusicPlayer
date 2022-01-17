package com.example.emotionbasedmusic.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.MusicListItemGridBinding
import com.example.emotionbasedmusic.databinding.MusicListItemViewBinding
import com.example.emotionbasedmusic.fragments.ResultSongsFragment
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.squareup.picasso.Picasso

class MusicAdapter(private val iPost: IPost, private val context: Context): ListAdapter<Music, MusicAdapter.MusicViewHolder>(DiffCallback) {

    private var index : Int = -1
    private var songPlaying: Boolean = false
    interface IPost {
        fun onPlay(songUri: String)
        fun onPauseMusic()
    }

    class MusicViewHolder(binding: MusicListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
         val songName = binding.songName
         val artistName = binding.artistName
         val img = binding.songImgView
         val btnPlay = binding.btnPlay
         val cv = binding.cvSong
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            ResultSongsFragment.binding.pfDetect.pFrame.makeGone()
            ResultSongsFragment.binding.pfDetect.progressBarLayout.progressBar.makeGone()
            ResultSongsFragment.binding.cl1.makeGone()
            ResultSongsFragment.binding.cl2.makeVisible()
            val song = getItem(position)
            holder.songName.text = song.songName
            holder.artistName.text = song.artistName
            if(song.playing) {
                holder.cv.strokeColor = context.getColor(R.color.border_color)
                holder.btnPlay.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pause_24))
            }
             else {
                holder.cv.strokeColor = context.getColor(R.color.white)
                holder.btnPlay.setImageDrawable(context.getDrawable(R.drawable.play_arrow_white))
            }
            holder.btnPlay.setOnClickListener {
                if(song.playing) {
                    song.playing = false
                    notifyItemChanged(position)
                    songPlaying = false
                    iPost.onPauseMusic()
                }
                else {
                    if(songPlaying) {
                        getItem(index).playing = false
                        notifyItemChanged(index)
                    }
                    else {

                    }
                    songPlaying = true
                    song.playing = true
                    notifyItemChanged(position)
                    index = position
                    iPost.onPlay(song.songUrl)
                }
            }
            Picasso.get().load(song.imgUrl).into(holder.img)
    }
}