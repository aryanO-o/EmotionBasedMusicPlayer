package com.example.emotionbasedmusic.helper


import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.emotionbasedmusic.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emotionbasedmusic.adapter.emojiAdapter
import com.example.emotionbasedmusic.dataSource.emojiData
import com.example.emotionbasedmusic.fragments.MoodRecognitionFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetDialog(
    private val detail: String?,
    private val fragment: Fragment,
    private val listener: IBottom?,
    private val textD: String?,
    private val sBottom: SBottom?,
    private val moodRecognitionFragment: MoodRecognitionFragment?
) : View.OnClickListener {

    private lateinit var etChange: EditText
    private lateinit var adapter: emojiAdapter
    interface IBottom {
        fun onCancelClick()
        fun onSaveClick(updatedDetail: String)
    }

    interface SBottom {
        fun onNoClick()
        fun onYesClick()
    }

    private lateinit var bottomSheetDialog: BottomSheetDialog

    fun initBottomSheet(layoutId: Int) {
        when (layoutId) {
            Constants.EDIT_BOTTOM -> {
                initEditBottom()
            }
            Constants.SIGN_OUT_BOTTOM -> {
                initSignOutBottom()
            }
            Constants.EMOJI_BOTTOM -> {
                initEmojiBottom()
            }
        }

    }

    private fun initEmojiBottom() {
        bottomSheetDialog = BottomSheetDialog(fragment.requireContext())
        bottomSheetDialog.setContentView(R.layout.emoji_bottom)
        val emojiDataSet = emojiData().loadEmoji();
        val rv = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvEmoji)
        rv?.layoutManager = LinearLayoutManager(fragment.requireContext(), LinearLayoutManager.HORIZONTAL, false)
        moodRecognitionFragment?.let {rv?.adapter = emojiAdapter(it, emojiDataSet)  }
        bottomSheetDialog.show()
    }

    private fun initSignOutBottom() {
        bottomSheetDialog = BottomSheetDialog(fragment.requireContext())
        bottomSheetDialog.setContentView(R.layout.sign_out_bottom)
        val btnNo = bottomSheetDialog.findViewById<View>(R.id.btnNo)
        val btnYes = bottomSheetDialog.findViewById<View>(R.id.btnYes)
        btnNo?.setOnClickListener(this)
        btnYes?.setOnClickListener(this)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    private fun initEditBottom() {
        bottomSheetDialog = BottomSheetDialog(fragment.requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet)
        val text = bottomSheetDialog.findViewById<View>(R.id.tvChangeDetails) as MaterialTextView
        text.text = detail
        etChange = bottomSheetDialog.findViewById<View>(R.id.etChangeDetails) as EditText
        etChange.setText(textD)
        etChange.setSelection(textD?.length!!)
        val btnCancel = bottomSheetDialog.findViewById<View>(R.id.btnCancel)
        val btnSave = bottomSheetDialog.findViewById<View>(R.id.btnSave)
        showKeyboard(etChange)
        bottomSheetDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        btnCancel!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        bottomSheetDialog.show()
    }

    private fun showKeyboard(etChange: EditText) {
        etChange.requestFocus()
        val imm: InputMethodManager? =
            fragment.requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        fragment.requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    fun hideKeyboard() {
        val imm: InputMethodManager? =
            fragment.requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnCancel -> {
                listener?.onCancelClick()
            }
            R.id.btnSave -> {
                listener?.onSaveClick(getUpdate())
            }
            R.id.btnNo -> {
                sBottom?.onNoClick()
            }
            R.id.btnYes -> {
                sBottom?.onYesClick()
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