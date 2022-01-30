package com.example.emotionbasedmusic.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.Music
import com.example.emotionbasedmusic.data.UserInfo
import com.example.emotionbasedmusic.fragments.MoodRecognitionFragment
import com.example.emotionbasedmusic.fragments.ResultSongsFragment
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.network.API
import com.example.emotionbasedmusic.services.MusicService
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class MusicViewModel(private var fragment: Fragment): ViewModel(), ValueEventListener {
    private var bitmap: Bitmap? = null
    private lateinit var mood: String
    var key: Boolean? = true
    val musicData = MutableLiveData<List<Music>>()
    private var song: Music? = null
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var ref: DatabaseReference
    var progressIndicator: Boolean = false
    val _likedSongs = MutableLiveData<MutableList<Music>>()
    val likedSongs = mutableListOf<Music>()
    var name = MutableLiveData("")
    var phone = MutableLiveData("")
    var email = MutableLiveData("")
    var imgUri = MutableLiveData("")
    var check = -1
    var aKey = -1
    private var child = ""
    private var uri = ""
    init {
        getLikedSongs()
    }
    fun parcelData(account: FirebaseUser) {
        val user = UserInfo(account.displayName!!, account.email!!, account.phoneNumber?:"", account.photoUrl!!.toString())
        updateData(user)
    }

    fun getData() {
        ref = database.reference.child(Constants.USERS).child(auth.currentUser!!.uid)
        viewModelScope.launch {
            ref.addValueEventListener(this@MusicViewModel)
        }
    }

    fun setUri(uri: String) {
        this.uri = uri
    }

    fun getUri(): String {
        return uri
    }

    private fun updateData(user: UserInfo) {
        ref = database.reference.child(Constants.USERS).child(auth.currentUser!!.uid)

            ref.setValue(user).addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(task: Task<Void>) {
                    if(task.isSuccessful) {
                        Toast.makeText(fragment.requireContext(), R.string.successful_msg, Toast.LENGTH_SHORT).show()
                        MoodRecognitionFragment.binding.pIndicator.makeGone()
                    }
                    else {
                        Toast.makeText(fragment.requireContext(), R.string.upload_error, Toast.LENGTH_SHORT).show()
                        MoodRecognitionFragment.binding.pIndicator.makeGone()
                    }
                    progressIndicator = false
                }

            })

    }

    fun setAdapterKey(key: Int) {
        aKey = key
    }

    fun setSong(song: Music) {
        this.song = song
    }


    fun getSong(): Music? {
        return if(song==null) {
            null
        } else {
            song
        }
    }
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun getMood(): String {
        return mood
    }

     fun getSongs(mood: String) {
         this.mood = mood
         musicData.value = mutableListOf()
         viewModelScope.launch {
             try {
                 when (mood) {
                     Constants.HAPPY_MOOD -> {
                         musicData.value = API.retrofitService.getHappySongs()
                     }
                     Constants.SAD_MOOD -> {
                         musicData.value = API.retrofitService.getSadSongs()
                     }
                     Constants.NEUTRAL_MOOD -> {
                         musicData.value = API.retrofitService.getNeutralSongs()
                     }
                 }
             } catch (e: Exception) {
                 ResultSongsFragment.binding.neResultSongs.ne.makeVisible()
             }
         }
    }

    fun addToLikedSongs(song: Music) {
        val ref = database.reference.child(Constants.LIKED_SONGS).child(auth.currentUser!!.uid)
        viewModelScope.launch {
            ref.push().setValue(song)
        }
    }

    fun removedFromLikedSongs(song:Music) {
        val ref = database.reference.child(Constants.LIKED_SONGS).child(auth.currentUser!!.uid)
        val query = ref.orderByChild("songName").equalTo(song.songName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap: DataSnapshot in snapshot.children) {
                    viewModelScope.launch {
                        likedSongs.remove(song)
                        _likedSongs.value!!.remove(song)
                        snap.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getLikedSongs() {
        val ref = database.reference.child(Constants.LIKED_SONGS).child(auth.currentUser!!.uid)
        _likedSongs.value = mutableListOf()
        resetLikedSongs()
        viewModelScope.launch {
            ref.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(data: DataSnapshot in snapshot.children) {
                        val song = data.getValue(Music::class.java)
                        if(!(likedSongs.contains(song))) {
                            likedSongs.add(song!!)
                        }
                    }
                    _likedSongs.value = likedSongs
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun resetLikedSongs() {
        likedSongs.clear()
        _likedSongs.value!!.clear()
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        name.value = snapshot.child("name").value.toString()
        phone.value = snapshot.child("phone").value.toString()
        imgUri.value = snapshot.child("imgUri").value.toString()
        email.value = snapshot.child("email").value.toString()
    }

    override fun onCancelled(error: DatabaseError) {

    }

    fun updateDetails(update: String) {
        when(check) {
                0 -> {
                   this.child = Constants.NAME
                }

                1-> {
                    this.child = Constants.PHONE
                }

                2 -> {
                  this.child = Constants.EMAIL
                }
            }
        ref = database.reference.child(Constants.USERS).child(auth.currentUser!!.uid).child(child)
        viewModelScope.launch {
            ref.setValue(update)
        }
    }
}


class MusicViewModelFactory(private val fragment: Fragment): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(fragment) as T
        }
        throw IllegalStateException("Unknown error")
    }

}



