package com.example.emotionbasedmusic.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.ProgressBarBinding

import com.example.emotionbasedmusic.databinding.SignUpFragmentBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.makeGone
import com.example.emotionbasedmusic.helper.makeVisible
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


class SignUpFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: SignUpFragmentBinding
    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignUpFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        binding.apply {
            btnLoginGoogle.setOnClickListener(this@SignUpFragment)
            btnLoginPhone.setOnClickListener(this@SignUpFragment)
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

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnLoginGoogle.id -> {
                loginThroughGoogle()
            }
            binding.btnLoginPhone.id -> {
                loginWithPhone()
            }
        }
    }

    private fun loginWithPhone() {
        toPhoneFragment()
    }

    private fun toPhoneFragment() {
        findNavController().navigate(R.id.action_signUpFragment_to_phoneFragment)
    }

    private fun loginThroughGoogle() {
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, Constants.GOOGLE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Constants.GOOGLE_REQUEST_CODE) {
            progressFrameVisible()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {

        try {
            val account = task?.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        }
        catch (e: ApiException) {
            progressFrameGone()
            Toast.makeText(requireContext(),getString(R.string.error_msg), Toast.LENGTH_SHORT).show()
        }
    }

    private fun progressFrameVisible() {
        binding.apply {
            pfSignUp.pFrame.makeVisible()
            pfSignUp.progressBarLayout.progressBar.makeVisible()
            tvPleaseWait.makeVisible()
        }
    }

    private fun progressFrameGone() {
        binding.apply {
            pfSignUp.pFrame.makeGone()
            pfSignUp.progressBarLayout.progressBar.makeGone()
            tvPleaseWait.makeGone()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task->
                if(task.isSuccessful) {
                    if(task.result.additionalUserInfo?.isNewUser ==true) {
                        model.progressIndicator = true
                        val account = task.result.user
                        model.parcelData(account!!)
                        Toast.makeText(requireContext(), "Setting up", Toast.LENGTH_SHORT).show()
                        toFaceScanFragment()
                    }
                    else {
                        Toast.makeText(requireContext(), "Welcome Back", Toast.LENGTH_SHORT).show()
                        toFaceScanFragment()
                    }
                }
        }
    }

    private fun toFaceScanFragment() {
        findNavController().navigate(R.id.action_signUpFragment_to_moodRecognitionFragment)
    }

}


