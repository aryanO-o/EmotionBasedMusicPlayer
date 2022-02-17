package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.adapter.SongImageAdapter
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.databinding.NewMusicFragmentBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.ScrollListenerHelper
import com.example.emotionbasedmusic.services.MusicService
import com.example.emotionbasedmusic.viewModel.MusicViewModel

class MusicFragment : Fragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener,
    ServiceConnection, ScrollListenerHelper.OnSnapPositionChangeListener {

    private val model: MusicViewModel by activityViewModels()
    private var song: Music? = null
    private var intent: Intent? = null
    private var notificationManager: NotificationManager? = null
    private var songsList: MutableLiveData<List<Music>> = MutableLiveData<List<Music>>()
    private var shuffledList = MutableLiveData<MutableList<Music>>()
    private var isLooping = false
    private var key: Boolean? = true
    private var service: MusicService? = null
    private var isBounded: Boolean? = false
    private var isFromFavorite: Boolean = false
    val _likedSongs = MutableLiveData<MutableList<Music>>()
    private var pagerSnapHelper: PagerSnapHelper? = null
    private var recyclerView: RecyclerView? = null
    private var snapPosition = 0
    private var prevPosition = 0
    private var scrollListenerHelper: ScrollListenerHelper? = null
    private var songImageAdapter: SongImageAdapter? = null
    private var urlList = mutableListOf<String>()
    private var shuffledLikedSongs = mutableListOf<Music>()
    private var shuffledLikedSongUrl = mutableListOf<String>()
    companion object {
        lateinit var binding: NewMusicFragmentBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewMusicFragmentBinding.inflate(inflater)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        key = (requireActivity() as MainActivity).key
        isFromFavorite = (requireActivity() as MainActivity).isFromFavorite
        shuffledList.value = mutableListOf()
        notificationManager =
            requireActivity().getSystemService(NotificationManager::class.java) as NotificationManager
        binding.sBar.setOnSeekBarChangeListener(this)
        songsList.value = mutableListOf()
        _likedSongs.value = mutableListOf()
        model.musicData.observe(viewLifecycleOwner) {
            this.songsList.value = it
            setUpRecyclerViewWithPager()
        }
        model._likedSongs.observe(viewLifecycleOwner) {
            this._likedSongs.value = it

        }
        this.song = model.getSong()
        song?.let { (requireActivity() as MainActivity).setSong(it) }
        songsList.value?.let { (requireActivity() as MainActivity).setSongList(it) }
        checkForKey()
        binding.apply {
            btnBack.setOnClickListener(this@MusicFragment)
            btnLike.setOnClickListener(this@MusicFragment)
            btnNextSong.setOnClickListener(this@MusicFragment)
            btnPlay.setOnClickListener(this@MusicFragment)
            ibLoop.setOnClickListener(this@MusicFragment)
            btnPreviousSong.setOnClickListener(this@MusicFragment)
        }
    }

    private fun getShuffledList(): MutableList<String> {
        song?.let {
            urlList.add(it.imgUrl)
            shuffledList.value?.add(it)
        }
        songsList.value!!.forEach { music ->
            if (music != song) {
                urlList.add(music.imgUrl)
                shuffledList.value?.add(music)
            }
        }
        this.songsList.value = shuffledList.value
        return urlList
    }

    private fun setUpRecyclerViewWithPager() {
        pagerSnapHelper = PagerSnapHelper()
        songImageAdapter = SongImageAdapter(requireContext())
        songImageAdapter?.bindList(getShuffledList())
        binding.rvMusic.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            pagerSnapHelper?.attachToRecyclerView(this)
            adapter = songImageAdapter
        }
        pagerSnapHelper?.let { scrollListenerHelper = ScrollListenerHelper(this, it, snapPosition) }
        scrollListenerHelper?.let { binding.rvMusic.addOnScrollListener(it) }
        service?.setSongsList(this.songsList.value!!)
        service?.setImageUrlList(urlList)
    }

    private fun getUrlList(): List<String> {
        songsList.value!!.forEach { music ->
            urlList.add(music.imgUrl)
        }
        return urlList
    }

    private fun getList(): MutableList<Int> {
        var list = mutableListOf<Int>()
        for (i in 1..8) {
            list.add(R.drawable.album_cover)
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSnapPositionChange(position: Int) {
        service?.setTo(position)
        prevPosition = position
        service?.setPosition(position)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkForLiked() {
        if (key!!) {

        } else {
            this.song = service?.getSong()
        }
        when (_likedSongs.value!!.contains(song)) {
            true -> {
                binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_outline_favorite_24))
            }
            false -> {
                binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
            }
        }
    }

    private fun checkForKey() {
        when (key!!) {
            true -> {
                (requireActivity() as MainActivity).stopForegroundService()
                initView()
            }
            false -> {
                checkForService()
            }
        }
    }

    private fun checkForService() {
        bindToService()
    }

    private fun bindToService() {
        Intent(requireContext(), MusicService::class.java).also {
            requireActivity().bindService(it, this, Context.BIND_AUTO_CREATE)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        binding.apply {
            tvStartTime.text = getString(R.string.tools_text)
            tvEndTime.text = getText(R.string.tools_text)
            sBar.progress = 0
            btnPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24_b))
            tvSongName.text = song?.songName
            tvArtist.text = song?.artistName
        }
        startMusicService()
    }

    private fun startMusicService() {
        (requireActivity() as MainActivity).startService()
        bindToService()
    }

    private fun pauseMediaPlayer() {
        service?.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyResources()
    }

    private fun destroyResources() {
        requireActivity().unbindService(this)
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPlay -> {
                when (MusicService.mediaPlayer.isPlaying) {
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
            R.id.ibLoop -> {
                if (isLooping) {
                    isLooping = false
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.repeat_icon))
                    setLooping(isLooping)
                } else {
                    isLooping = true
                    binding.ibLoop.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_repeat_24_b))
                    setLooping(isLooping)
                }
            }
            R.id.btnNextSong -> {
                nextSong()
            }
            R.id.btnPreviousSong -> {
                prevSong()
            }
            R.id.btnLike -> {
                checkForFavorite()
            }
            R.id.btnBack -> {
                popBackStack()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextSong() {
        service?.nextSong()
        getSong()
        checkForLiked()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun prevSong() {
        service?.prevSong()
        getSong()
        checkForLiked()
    }

    private fun getSong() {
        this.song = service?.getSong()
    }

    private fun popBackStack() {
        (requireActivity() as MainActivity).navController.popBackStack()
    }

    private fun checkForFavorite() {
        if (key!!) {

        } else {
            this.song = service?.getSong()
        }
        if (this._likedSongs.value!!.contains(song)) {
            removeFromLiked(song!!)
        } else {
            addToLiked(song!!)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun removeFromLiked(song: Music) {
        binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_favorite_border_24_4))
        model.removedFromLikedSongs(song)
        showToast(Constants.REMOVED_LIKED_SONGS)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addToLiked(song: Music) {
        binding.btnLike.setImageDrawable(resources.getDrawable(R.drawable.ic_outline_favorite_24))
        model.addToLikedSongs(song)
        showToast(Constants.ADDED_LIKED_SONGS)
    }

    private fun setLooping(looping: Boolean) {
        service?.setLooping(looping)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeMediaPlayer() {
        service?.resumeMediaPlayer()
    }

    override fun onProgressChanged(sb: SeekBar?, progress: Int, p2: Boolean) {

    }

    private fun seekTo(seekPosition: Int) {
        service?.seekTo(seekPosition)
    }

    override fun onStartTrackingTouch(sb: SeekBar?) {

    }

    override fun onStopTrackingTouch(sb: SeekBar?) {
        val progress = sb?.progress
        seekTo(progress!!)
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.LocalBinder
        this.service = binder.getService()
        isBounded = true
        when (key!!) {
            false -> {
                this.songsList.value = this.service?.getSongsList()
                this.urlList = this.service?.getImageUrlList() as MutableList<String>
                updateRecyclerView()
                checkForLiked()
                this.service?.getPagerPosition()?.let { binding.rvMusic.scrollToPosition(it)}
                this.service?.setUI()
            }
            true -> {
                checkForLiked()
                when (isFromFavorite) {
                    true -> {
                        this.service?.setSongsList(this._likedSongs.value!!)
                    }
                    false -> {
                        this.service?.recyclerViewPriority(true)
                        this.service?.setSongsList(this.songsList.value!!)
                        this.service?.setImageUrlList(urlList)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        pagerSnapHelper = PagerSnapHelper()
        songImageAdapter = SongImageAdapter(requireContext())
        songImageAdapter?.bindList(getShuffledList())
        binding.rvMusic.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            pagerSnapHelper?.attachToRecyclerView(this)
            adapter = songImageAdapter
        }
        pagerSnapHelper?.let { scrollListenerHelper =
            this.service?.getPagerPosition()?.let { it1 -> ScrollListenerHelper(this, it, it1) }
        }
        scrollListenerHelper?.let { binding.rvMusic.addOnScrollListener(it) }
        service?.setSongsList(this.songsList.value!!)
        service?.setImageUrlList(urlList)
    }


    override fun onServiceDisconnected(p0: ComponentName?) {
        isBounded = false
    }

}

