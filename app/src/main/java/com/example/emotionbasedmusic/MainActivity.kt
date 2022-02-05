package com.example.emotionbasedmusic

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.emotionbasedmusic.fragments.MusicFragment
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.services.MusicService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    var isFromNotification: Boolean = false
    var isServiceRunning: Boolean = false
    var key: Boolean? = true
    private lateinit var activityManager: ActivityManager
    var isFromFavorite = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.isFromNotification =
            intent?.extras?.getBoolean(Constants.IS_FROM_NOTIFICATION) ?: false
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    override fun onBackPressed() {
        when (navController.currentDestination!!.label) {
            Constants.FRAGMENT_MOOD_RECOGNITION -> {
                finish()
            }
            Constants.SIGN_UP_FRAGMENT -> {
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (getListOfServices()) {
            moveToMusic()
        }
    }

    private fun getListOfServices(): Boolean {
        for (service: ActivityManager.RunningServiceInfo in activityManager.getRunningServices(
            Integer.MAX_VALUE
        )) {
            if (service.service.shortClassName == Constants.MUSIC_SERVICE) {
                return true
            }
        }
        return false
    }

    fun moveToMusic() {
        key = false
        if (navController.currentDestination!!.label == Constants.FRAGMENT_MUSIC) {
            navController.popBackStack()
        }
        navController.navigate(R.id.musicFragment)
    }

}