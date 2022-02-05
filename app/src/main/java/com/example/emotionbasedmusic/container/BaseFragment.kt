package com.example.emotionbasedmusic.container

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.emotionbasedmusic.eventBus.MessageEvent
import com.example.emotionbasedmusic.helper.Constants
import org.greenrobot.eventbus.EventBus

open class BaseFragment : Fragment() {

    fun requestPermForWrite() {
        requestPermissions(
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Constants.WRITE_REQUEST_CODE
        )
    }

    fun requestPermForCam() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            Constants.CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.size == 1) {
                Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT)
                    .show()
                EventBus.getDefault().post(MessageEvent(Constants.EXECUTE_CAMERA_PERM))
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == Constants.WRITE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.size == 1) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
                EventBus.getDefault().post(MessageEvent(Constants.EXECUTE_WRITE_PERM))
            } else {
                Toast.makeText(requireContext(), "permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}