package com.example.emotionbasedmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.emotionbasedmusic.helper.Constants

class MainActivity : AppCompatActivity() {
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    var isFromNotification: Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.isFromNotification = intent?.extras?.getBoolean(Constants.IS_FROM_NOTIFICATION)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController
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
}