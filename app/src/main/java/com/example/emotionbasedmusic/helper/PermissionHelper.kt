package com.example.emotionbasedmusic.helper

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import java.util.*


class PermissionHelper(private val activity: Activity) {
    interface Listener {
        fun onPermissionGranted(permission: String?, requestCode: Int)
        fun onPermissionDenied(permission: String?, requestCode: Int)
        fun onPermissionDeniedPermanent(permission: String?, requestCode: Int)
    }

    private val listeners: MutableSet<Listener> = HashSet()
    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun getListeners(): Set<Listener> {
        return Collections.unmodifiableSet(listeners)
    }


    fun onPermissionResult(requestCode: Int, permissions: Array<out String>, grantResult: IntArray) {
        if (permissions.isEmpty()) throw RuntimeException("no permission on request result")
        val permission = permissions[0]
        if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            notifyPermissionGranted(permission, requestCode)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                notifyPermissionDenied(permission, requestCode)
            } else {
                notifyPermissionDeniedPermanently(permission, requestCode)
            }
        }
    }

    private fun notifyPermissionDeniedPermanently(permission: String?, requestCode: Int) {
        for (listener in getListeners()) {
            listener.onPermissionDeniedPermanent(permission, requestCode)
        }
    }

    private fun notifyPermissionDenied(permission: String?, requestCode: Int) {
        for (listener in getListeners()) {
            listener.onPermissionDenied(permission, requestCode)
        }
    }

    private fun notifyPermissionGranted(permission: String?, requestCode: Int) {
        for (listener in getListeners()) {
            listener.onPermissionGranted(permission, requestCode)
        }
    }

    fun isPermissionGranted(permission: String?): Boolean {
        return (ActivityCompat.checkSelfPermission(activity, permission!!) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestPermission(permissions: Array<out String>, requestCode: Int) {
        requestPermissions(activity, permissions, requestCode)
    }
}
