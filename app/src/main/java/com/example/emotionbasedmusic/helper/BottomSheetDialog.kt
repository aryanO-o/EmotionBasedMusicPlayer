package com.example.emotionbasedmusic.helper

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import com.example.emotionbasedmusic.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView

class BottomSheetDialog(private val detail: String, private val context: Context, private val listener: IBottom, private val textD: String): View.OnClickListener {

    private lateinit var etChange: EditText
    interface IBottom {
        fun onCancelClick()
        fun onSaveClick(updatedDetail: String)
    }
    private lateinit var bottomSheetDialog : BottomSheetDialog

   fun initBottomSheet() {
       bottomSheetDialog = BottomSheetDialog(context)
       bottomSheetDialog.setContentView(R.layout.bottom_sheet)
       val text = bottomSheetDialog.findViewById<View>(R.id.tvChangeDetails) as MaterialTextView
       text.text = detail
       etChange = bottomSheetDialog.findViewById<View>(R.id.etChangeDetails) as EditText
       etChange.setText(textD)
       etChange.setSelection(textD.length)
       val btnCancel = bottomSheetDialog.findViewById<View>(R.id.btnCancel)
       val btnSave = bottomSheetDialog.findViewById<View>(R.id.btnSave)
       bottomSheetDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
       btnCancel!!.setOnClickListener(this)
       btnSave!!.setOnClickListener(this)
       bottomSheetDialog.show()
   }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnCancel -> {
                listener.onCancelClick()
            }
            R.id.btnSave -> {
                listener.onSaveClick(getUpdate())
            }
        }
    }

    private fun getUpdate(): String {
        return etChange.text.toString().trim()
    }

    fun dismiss() {
        bottomSheetDialog.dismissWithAnimation = true
        bottomSheetDialog.dismiss()
    }

}