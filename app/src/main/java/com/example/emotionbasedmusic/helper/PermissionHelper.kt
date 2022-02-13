package com.example.emotionbasedmusic.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.container.BaseFragment
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.emotionbasedmusic.eventBus.MessageEvent

import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class PermissionHelper(private val activity: Activity) : BaseFragment() {

}
