package com.example.emotionbasedmusic.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels

import androidx.recyclerview.widget.RecyclerView

import androidx.navigation.fragment.findNavController

import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.adapter.emojiAdapter
import com.example.emotionbasedmusic.dataSource.emojiData
import com.example.emotionbasedmusic.databinding.FragmentFaceProceedOrRetakeBinding
import com.example.emotionbasedmusic.databinding.FragmentMoodRecognitionBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.Dialog
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MoodRecognitionFragment : Fragment(), View.OnClickListener, Dialog.IListener {
    private lateinit var binding: FragmentMoodRecognitionBinding
    private val model: MusicViewModel by activityViewModels()
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: Dialog
    private var bitmap: Bitmap? = null
    private var isFromGallery: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMoodRecognitionBinding.inflate(inflater)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val emojiDataSet = emojiData().loadEmoji();
        val emojiRecyclerView = view?.findViewById<RecyclerView>(R.id.emoji_recycler_view)
        emojiRecyclerView?.adapter = emojiAdapter(this.requireContext(), emojiDataSet);

        initToolbar()
        initData()
        binding.apply {
            btnAddImage.setOnClickListener(this@MoodRecognitionFragment)
        }
    }

    private fun initData() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
    }


    private fun initToolbar() {
        binding.apply {
            tbSignUp.tbCommon.makeVisible()
            tbSignUp.tbCommon.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item?.itemId) {
                        R.id.sign_out -> {signOut()}
                    }
                    return true
                }
            })
        }
    }

    private fun signOut() {
        googleSignInClient.signOut()
        auth.signOut()
        toCheckFragment()
    }

    private fun toCheckFragment() {
        findNavController().navigate(R.id.action_moodRecognitionFragment_to_checkFragment)

    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnAddImage -> {showDialog()}
        }
    }

    private fun showDialog() {
        dialog = Dialog(requireContext(), this)
        dialog.showDialog()
    }

    override fun btnCamera() {
        dismissDialog()
        openCamera()
    }

    private fun openCamera() {
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.CAMERA), Constants.CAMERA_PERMISSION_CODE)
        }
    }

    override fun btnGallery() {
        dismissDialog()
        galleryCall()
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent?  = result.data
            if(data!=null) {
                isFromGallery = true
                toFaceProceedFragment(bitmap!!, data.data!!.toString(), isFromGallery)
            }

        }
    }

    private fun startCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE)
    }

    private fun dismissDialog() {
        dialog.dismissDialog()
    }

    private fun toFaceProceedFragment(bitmap: Bitmap, uri: String?, boolean: Boolean) {
        val action = MoodRecognitionFragmentDirections.actionMoodRecognitionFragmentToFaceProceedOrRetakeFragment(uri, boolean, bitmap)
        findNavController().navigate(action)
    }

    private fun galleryCall() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcher.launch(Intent.createChooser(intent, "Choose Image"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            isFromGallery = false
            toFaceProceedFragment(bitmap!!, null, isFromGallery)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==Constants.CAMERA_PERMISSION_CODE) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults.size==1) {
                Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT).show()
                startCamera()
            }
            else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}