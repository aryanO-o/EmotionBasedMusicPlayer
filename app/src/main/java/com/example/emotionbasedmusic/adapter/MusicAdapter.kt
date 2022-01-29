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
import com.example.emotionbasedmusic.databinding.FavoritesItemViewBinding
import com.example.emotionbasedmusic.databinding.MusicListItemGridBinding
import com.example.emotionbasedmusic.databinding.MusicListItemViewBinding
import com.example.emotionbasedmusic.fragments.ResultSongsFragment
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.squareup.picasso.Picasso

class MusicAdapter(private val iPost: IPost?, private val context: Context, private var check: Boolean, private val adapterKey: Int, private val iFavorite: IFavorite?): ListAdapter<Music, RecyclerView.ViewHolder>(DiffCallback) {

    private var index : Int = -1
    private var songPlaying: Boolean = false
    interface IPost {
        fun onPlay(song: Music, position: Int)
        fun onPauseMusic()
        fun onItemSongClick(song: Music)
    }

    interface IFavorite {
        fun onRemoveClick(song: Music)
        fun onItemSongClick(song: Music)
    }

    class MusicViewHolder(binding: MusicListItemViewBinding): RecyclerView.ViewHolder(binding.root) {
         val songName = binding.songName
         val artistName = binding.artistName
         val img = binding.songImgView
    }

    class FavoriteViewHolder(val binding: FavoritesItemViewBinding): RecyclerView.ViewHolder(binding.root) {
        val songN = binding.songName
        val artistN = binding.artistName
        val img = binding.songImgView
        val btnRemove = binding.btnRemove
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> {
                val adapterLayout = MusicListItemViewBinding.inflate(LayoutInflater.from(parent.context))
                MusicViewHolder(adapterLayout)
            }
            else-> {
                val adapterLayout = FavoritesItemViewBinding.inflate(LayoutInflater.from(parent.context))
                FavoriteViewHolder(adapterLayout)
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
//            when(check) {
//                true ->{
//                    ResultSongsFragment.binding.pfDetect.pFrame.makeGone()
//                    ResultSongsFragment.binding.pfDetect.progressBarLayout.progressBar.makeGone()
//                    ResultSongsFragment.binding.cl1.makeGone()
//                    ResultSongsFragment.binding.cl2.makeVisible()
//                }
//                false -> {}
//            }
//            val song = getItem(position)
//            holder.songName.text = song.songName
//            holder.artistName.text = song.artistName
//            if(song.playing) {
//                holder.cv.strokeColor = context.getColor(R.color.border_color)
//                holder.btnPlay.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pause_24))
//            }
//             else {
//                holder.cv.strokeColor = context.getColor(R.color.white)
//                holder.btnPlay.setImageDrawable(context.getDrawable(R.drawable.play_arrow_white))
//            }
//            holder.btnPlay.setOnClickListener {
//                if(song.playing) {
//                    song.playing = false
//                    notifyItemChanged(position)
//                    songPlaying = false
//                    iPost.onPauseMusic()
//                }
//                else {
//                    if(songPlaying) {
//                        getItem(index).playing = false
//                        notifyItemChanged(index)
//                    }
//                    else {
//
//                    }
//                    songPlaying = true
//                    song.playing = true
//                    notifyItemChanged(position)
//                    index = position
//                    iPost.onPlay(song, position)
//                }
//            }
//            holder.itemView.setOnClickListener {
//                iPost.onItemSongClick(song)
//            }
//            Picasso.get().load(song.imgUrl).into(holder.img)
//    }

    override fun getItemViewType(position: Int): Int {
        return adapterKey
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.javaClass == MusicViewHolder::class.java) {
            true -> {
                bindMusic(holder, position)
            }
            false -> {
                bindFavorites(holder, position)
            }
        }
    }

    private fun bindFavorites(holder: RecyclerView.ViewHolder, position: Int) {
        val favoriteHolder = holder as FavoriteViewHolder
        val song = getItem(position)
        favoriteHolder.apply {
            songN.text = song.songName
            artistN.text = song.artistName
            Picasso.get().load(song.imgUrl).into(img)
            btnRemove.setOnClickListener {
                iFavorite?.onRemoveClick(song)
            }
            itemView.setOnClickListener {
                iFavorite?.onItemSongClick(song)
            }
        }
    }

    private fun bindMusic(holder: RecyclerView.ViewHolder, position: Int) {
        val musicHolder = holder as MusicViewHolder
        val song = getItem(position)
        when(check) {
            true ->{
                ResultSongsFragment.binding.pfDetect.pFrame.makeGone()
                ResultSongsFragment.binding.pfDetect.progressBarLayout.progressBar.makeGone()
                ResultSongsFragment.binding.cl1.makeGone()
                ResultSongsFragment.binding.cl2.makeVisible()
                this.check = false
            }
            false -> {}
        }
        musicHolder.apply {
            songName.text = song.songName
            artistName.text = song.artistName
            Picasso.get().load(song.imgUrl).into(img)
            itemView.setOnClickListener {
                iPost?.onItemSongClick(song)
            }
        }
    }
}