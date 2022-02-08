package com.example.emotionbasedmusic.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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


class PermissionHelper (private val activity: Activity) {

    fun checkForPerm(perm: String) {
        when (perm) {
            Constants.WRITE_PERM -> {
                checkForWritePerm()
            }
            Constants.CAMERA_PERM -> {
                checkForCameraPerm()
            }
        }
    }

    private fun checkForCameraPerm() {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            EventBus.getDefault().post(MessageEvent(Constants.EXECUTE_CAMERA_PERM))
        } else {
            requestPermForCam()
        }
    }

    private fun checkForWritePerm() {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            EventBus.getDefault().post(MessageEvent(Constants.EXECUTE_WRITE_PERM))
        } else {
            requestPermForWrite()
        }
    }

    private fun requestPermForWrite() {
        requestPermissions(activity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Constants.WRITE_REQUEST_CODE
        )
    }

    private fun requestPermForCam() {
        requestPermissions(activity,
            arrayOf(android.Manifest.permission.CAMERA),
            Constants.CAMERA_PERMISSION_CODE
        )
    }
}
