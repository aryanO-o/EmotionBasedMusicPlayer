package com.example.emotionbasedmusic.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.UsersProfileBinding
import com.example.emotionbasedmusic.helper.BottomSheetDialog
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), View.OnClickListener, BottomSheetDialog.IBottom {
    private lateinit var binding: UsersProfileBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var detail = ""
    private var textD = ""
    private var phone = ""
    private var name = ""
    private var email = ""
    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UsersProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        checkPermission()
        binding.apply {
            userEmailCard.setOnClickListener(this@ProfileFragment)
            userNameCard.setOnClickListener(this@ProfileFragment)
            userPhoneCard.setOnClickListener(this@ProfileFragment)
            btnChange.setOnClickListener(this@ProfileFragment)
            btnNameEdit.setOnClickListener(this@ProfileFragment)
            btnPhoneEdit.setOnClickListener(this@ProfileFragment)
            btnEmailEdit.setOnClickListener(this@ProfileFragment)
        }
    }

    private fun request() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            Constants.CAMERA_P_C
        )
    }

    private fun checkPermission() {
        when (ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            true -> {
                binding.btnChange.makeGone()
                binding.tvCameraPerm.text = "Granted"
                binding.tvCameraPerm.setTextColor(resources.getColor(R.color.green))
            }
            false -> {

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.CAMERA_P_C) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.size == 1) {
                Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
            checkPermission()
        }
    }

    private fun initView() {
        binding.apply {
            model.email.observe(viewLifecycleOwner) {
                userEmailText.text = it
                email = it
            }
            model.phone.observe(viewLifecycleOwner) {
                userPhoneText.text = it
                phone = it
            }
            model.name.observe(viewLifecycleOwner) {
                userNameText.text = it
                name = it
            }
            model.imgUri.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    it?.let { url -> Picasso.get().load(url).into(userProfilePhoto) }
                }
            }
        }
    }

    fun setNameDetails() {
        this.detail = Constants.NAME_DETAIL
        this.textD = name
        model.check = 0
        initBottomSheet()
    }

    fun setEmailDetails() {
        this.detail = Constants.EMAIL_DETAIL
        this.textD = email
        model.check = 2
        initBottomSheet()
    }

    fun setPhoneDetails() {
        this.detail = Constants.PHONE_DETAIL
        this.textD = phone
        model.check = 1
        initBottomSheet()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.user_name_card -> {
                setNameDetails()
            }
            R.id.user_email_card -> {
                setEmailDetails()
            }
            R.id.user_phone_card -> {
                setPhoneDetails()
            }
            R.id.btn_email_edit -> {
                setEmailDetails()
            }
            R.id.btn_phone_edit -> {
                setPhoneDetails()
            }
            R.id.btn_name_edit -> {
                setNameDetails()
            }
            R.id.btnChange -> {
                request()
            }
        }

    }

    private fun initBottomSheet() {
        bottomSheetDialog =
            BottomSheetDialog(this.detail, requireParentFragment(), this, textD, null)
        bottomSheetDialog.initBottomSheet(Constants.EDIT_BOTTOM)
    }

    override fun onCancelClick() {
        bottomSheetDialog.dismiss()
        bottomSheetDialog.hideKeyboard()
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveClick(updatedDetail: String) {
        model.updateDetails(updatedDetail)
        bottomSheetDialog.dismiss()
        bottomSheetDialog.hideKeyboard()
    }

}