package com.example.emotionbasedmusic.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.emotionbasedmusic.MainActivity
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.container.AppContainer
import com.example.emotionbasedmusic.data.UserInfo
import com.example.emotionbasedmusic.databinding.UsersDataFragmentBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.Dialog
import com.example.emotionbasedmusic.helper.PermissionHelper
import com.example.emotionbasedmusic.viewModel.MusicViewModel

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class UsersDataFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var appContainer: AppContainer

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var storage: FirebaseStorage


    private var binding: UsersDataFragmentBinding? = null
    private var phone: String? = null
    private var name: String = ""
    private var email: String = ""
    private var localUri: Uri? = null
    private var firebaseUri: Uri? = null
    private var storageRef: StorageReference? = null

    private val model: MusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
        appContainer.repo.initSharedPreferences()
        phone = appContainer.repo.getSharedPreferences(Constants.MOBILE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UsersDataFragmentBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.progressIndicator = true
        storage = FirebaseStorage.getInstance()
        storageRef = auth.currentUser?.uid?.let { storage.reference.child(it) }
        binding?.apply {
            userDataContinueBtn.setOnClickListener(this@UsersDataFragment)
            userDataProfileImg.setOnClickListener(this@UsersDataFragment)
        }
        initView()
    }

    private fun initView() {
        binding?.userDataPhoneNumber?.text = phone
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.user_data_continue_btn -> {
                validateData()
            }
            R.id.user_data_profile_img -> {
                galleryCall()
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    localUri = data.data!!
                    binding?.userDataProfileImg?.setImageURI(localUri)
                }
            }
        }

    private fun galleryCall() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcher.launch(Intent.createChooser(intent, "Choose Image"))
    }

    private fun validateData() {
        name = binding?.userDataEditFullName?.text.toString().trim()
        email = binding?.userDataEditEmail?.text.toString().trim()
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.user_data_required_info),
                Toast.LENGTH_SHORT
            ).show()
            return
        } else parcelAndContinue()
    }

    private fun parcelAndContinue() {
        if (localUri != null) {
            localUri?.let { model.fetchAndParcel(it, getUser()) }
        } else {
            model.parcelData(getUser())
            toFaceScanFragment()
        }
    }

    private fun toFaceScanFragment() {
        Toast.makeText(requireContext(), "Setting up", Toast.LENGTH_SHORT).show()
        appContainer.repo.setSharedPreferences(
            Constants.IS_PROFILE_COMPLETE,
            Constants.PROFILE_COMPLETE
        )
        findNavController().navigate(R.id.action_usersDataFragment_to_moodRecognitionFragment)
    }

    private fun getUser(): UserInfo {
        return UserInfo(
            name,
            email,
            phone ?: "",
            firebaseUri.toString()
        )
    }

}