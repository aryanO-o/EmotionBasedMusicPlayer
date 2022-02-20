package com.example.emotionbasedmusic.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.UsersProfileBinding
import com.example.emotionbasedmusic.helper.BottomSheetDialog
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.PermissionHelper
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.viewModel.MusicViewModel

import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), View.OnClickListener, BottomSheetDialog.IBottom, PermissionHelper.Listener,
    MainActivity.RequestPermissionEventListener {
    private lateinit var binding: UsersProfileBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var detail = ""
    private var textD = ""
    private var phone = ""
    private var name = ""
    private var email = ""
    private val model: MusicViewModel by activityViewModels()
    private var permissionHelper: PermissionHelper? = null
    private var uri: String? = null
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
        uri?.let{
            if(it.isEmpty()) {binding.userProfilePhoto.setImageDrawable(resources.getDrawable(R.drawable.person_icon))}
        }
        permissionHelper = PermissionHelper(requireActivity())
        checkPermission()
        binding.apply {
            userEmailCard.setOnClickListener(this@ProfileFragment)
            userNameCard.setOnClickListener(this@ProfileFragment)
            userPhoneCard.setOnClickListener(this@ProfileFragment)
            btnChange.setOnClickListener(this@ProfileFragment)
            btnNameEdit.setOnClickListener(this@ProfileFragment)
            btnPhoneEdit.setOnClickListener(this@ProfileFragment)
            btnEmailEdit.setOnClickListener(this@ProfileFragment)
            changePhotoIcon.setOnClickListener(this@ProfileFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        registerPermissionListener(this)
        permissionHelper?.registerListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterPermissionListener(this)
        permissionHelper?.unregisterListener(this)
    }

    private fun request() {
        permissionHelper?.requestPermission(arrayOf(Manifest.permission.CAMERA), Constants.CAMERA_PERMISSION_CODE)
    }

    private fun checkPermission() {
        if(permissionHelper?.isPermissionGranted(Manifest.permission.CAMERA)==true) {
            onPermissionGrantedSuccessfully()
        }
    }

    private fun onPermissionGrantedSuccessfully() {
        binding.btnChange.makeGone()
        binding.tvCameraPerm.text = "Granted"
        binding.tvCameraPerm.setTextColor(resources.getColor(R.color.green))
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
                    uri = it
                    it?.let { url -> Picasso.get().load(url).placeholder(R.drawable.person_icon).into(userProfilePhoto) }
                }
            }
        }
    }

    private fun setNameDetails() {
        this.detail = Constants.NAME_DETAIL
        this.textD = name
        model.check = 0
        initBottomSheet()
    }

    private fun setEmailDetails() {
        this.detail = Constants.EMAIL_DETAIL
        this.textD = email
        model.check = 2
        initBottomSheet()
    }

    private fun setPhoneDetails() {
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
            R.id.change_photo_icon -> {
                galleryCall()
            }
        }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    binding.userProfilePhoto.setImageURI(data.data)
                    model.updateImage(data.data!!)
                    showToast(getString(R.string.updating))
                }
            }
        }

    private fun galleryCall() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcher.launch(Intent.createChooser(intent, "Choose Image"))
    }

    private fun initBottomSheet() {
        bottomSheetDialog =
            BottomSheetDialog(this.detail, requireParentFragment(), this, textD, null, null)
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

    override fun onPermissionGranted(permission: String?, requestCode: Int) {
        when (requestCode) {
            Constants.CAMERA_PERMISSION_CODE -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_granted),
                    Toast.LENGTH_SHORT
                ).show()
                onPermissionGrantedSuccessfully()
            }
        }
    }

    override fun onPermissionDenied(permission: String?, requestCode: Int) {
        when (requestCode) {
            Constants.CAMERA_PERMISSION_CODE -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.camera_perm_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPermissionDeniedPermanent(permission: String?, requestCode: Int) {
        when (requestCode) {
            Constants.CAMERA_PERMISSION_CODE -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.camera_perm_denied_permanently),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun registerPermissionListener(listener: MainActivity.RequestPermissionEventListener) {
        (requireActivity() as MainActivity).registerPermissionListener(listener)
    }

    private fun unregisterPermissionListener(listener: MainActivity.RequestPermissionEventListener) {
        (requireActivity() as MainActivity).unregisterPermissionListener(listener)
    }

    override fun onRequestPermissionsResults(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionHelper?.onPermissionResult(requestCode, permissions, grantResults)
    }

}