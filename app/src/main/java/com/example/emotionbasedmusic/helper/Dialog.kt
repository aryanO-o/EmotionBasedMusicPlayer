package com.example.emotionbasedmusic.helper

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.emotionbasedmusic.R

class Dialog(private val context: Context, private val listener: IListener) {
    private lateinit var dialog: Dialog

    interface IListener {
        fun btnCamera()
        fun btnGallery()
    }

    fun showDialog() {
        dialog = Dialog(context)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_select_image_source)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val btnCamera = dialog.findViewById<ConstraintLayout>(R.id.btnCamera)
        val btnGallery = dialog.findViewById<ConstraintLayout>(R.id.btnGallery)
        val btnCancel = dialog.findViewById<ImageView>(R.id.btnClose)
        btnCamera.setOnClickListener {
            listener.btnCamera()
        }
        btnGallery.setOnClickListener {
            listener.btnGallery()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}