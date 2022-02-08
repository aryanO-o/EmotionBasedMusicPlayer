package com.example.emotionbasedmusic.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.data.AzureResponse
import com.example.emotionbasedmusic.data.Emotions
import com.example.emotionbasedmusic.data.ImageBody
import com.example.emotionbasedmusic.data.Moods
import com.example.emotionbasedmusic.databinding.FragmentFaceProceedOrRetakeBinding
import com.example.emotionbasedmusic.network.AZURE
import com.example.emotionbasedmusic.network.AzureApi
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.example.emotionbasedmusic.viewModel.MusicViewModelFactory
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException
import kotlin.math.max
import android.provider.MediaStore
import com.example.emotionbasedmusic.container.AppContainer
import com.example.emotionbasedmusic.eventBus.MessageEvent
import com.example.emotionbasedmusic.helper.*
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import org.greenrobot.eventbus.ThreadMode

import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class FaceProceedOrRetakeFragment : Fragment(), View.OnClickListener, Dialog.IListener {
    private var binding: FragmentFaceProceedOrRetakeBinding? = null
    private val model: MusicViewModel by activityViewModels {
        MusicViewModelFactory(requireParentFragment())
    }
    private val navArgs: FaceProceedOrRetakeFragmentArgs by navArgs()
    private var imageUri: String? = null
    private var boolean: Boolean = false
    private var bitmap: Bitmap? = null
    private lateinit var dialog: Dialog
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: StorageReference
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var storage: FirebaseStorage
    private var mood = ""
    private lateinit var finalUri: Uri
    private lateinit var testImage: InputImage
    private var max = -1.1

    @Inject
    lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = navArgs.uri
        boolean = navArgs.isFromGallery
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaceProceedOrRetakeBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initData() {
        auth = FirebaseAuth.getInstance()
        permissionHelper = PermissionHelper(requireActivity())
        storage = FirebaseStorage.getInstance()
        ref = storage.reference.child("allImages").child(auth.currentUser!!.uid)
    }

    private fun initView() {
        binding?.apply {
            if (boolean) {
                ivFaceScan.setImageURI(imageUri?.toUri())
            } else {
                ivFaceScan.setImageBitmap(model.getBitmap())
            }
            btnProceed.setOnClickListener(this@FaceProceedOrRetakeFragment)
            btnRetake.setOnClickListener(this@FaceProceedOrRetakeFragment)
            faceResultFragment.btnSearchSongs.setOnClickListener(this@FaceProceedOrRetakeFragment)
            noFaceDetectedFragment.btnHome.setOnClickListener(this@FaceProceedOrRetakeFragment)
            cl2.makeGone()
            cl3.makeGone()
            cl1.makeVisible()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    binding?.ivFaceScan?.setImageURI(data.data)
                    this.imageUri = data.data.toString()
                    boolean = true
                }

            }
        }

    private fun galleryCall() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcher.launch(Intent.createChooser(intent, "Choose Image"))
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnRetake -> {
                showDialog()
            }
            R.id.btnProceed -> {
//                detectFace()
                getImageUrl()
            }
            R.id.btnSearchSongs -> {
                model.getSongs(mood)
                toResultSongFragment()
            }
            R.id.btnHome -> {
                findNavController().navigate(R.id.action_faceProceedOrRetakeFragment_to_moodRecognitionFragment)
            }
        }
    }

    private fun detectFaceThroughAzure(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val imgBody = ImageBody(url)
                val task = AZURE.azureService.sendImage(imgBody)
                withContext(Dispatchers.Main) {
                    azureFaces(task.body())
                }
            } catch (e: ApiException) {
                withContext(Dispatchers.Main) {
                    makeViewsGone()
                    binding?.cl1?.makeVisible()
                    Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun azureFaces(body: List<AzureResponse>?) {
        if (body != null) {
            if (body.isEmpty()) {
                binding?.apply {
                    makeViewsGone()
                    cl4.makeVisible()
                }
            } else {
                binding?.apply {
                    makeViewsGone()
                    cl3.makeVisible()
                }
                analyzeAzureFace(body[0].faceAttributes.emotion)
            }
        } else {
            Toast.makeText(requireContext(), "null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeAzureFace(azureResponse: Moods) {
        azureResponse.apply {
            max = maxOf(anger, fear, happiness, neutral, sadness, surprise)
        }
        updateUI(max, azureResponse)
    }

    private fun updateUI(max: Double, emotions: Moods) {
        when (max) {
            emotions.sadness -> {
                setEmotion(R.drawable.sad_face, "Sad")
                this.mood = Constants.SAD_MOOD
            }
            emotions.happiness -> {
                setEmotion(R.drawable.happy_face, "Happy")
                this.mood = Constants.HAPPY_MOOD
            }
            emotions.neutral -> {
                setEmotion(R.drawable.neutral_face, "Neutral")
                this.mood = Constants.NEUTRAL_MOOD
            }
            emotions.anger -> {
                setEmotion(R.drawable.angry_face, "Angry")
                this.mood = Constants.ANGRY_MOOD
            }
            emotions.surprise -> {
                setEmotion(R.drawable.surprised_face, "Surprise")
                this.mood = Constants.SAD_MOOD
            }
            emotions.disgust -> {
                setEmotion(R.drawable.tired_face, "Tired")
                this.mood = Constants.SAD_MOOD
            }
        }
    }

    private fun getImageUrl() {
        when (boolean) {
            true -> {
                this.finalUri = imageUri?.toUri()!!
                getStorageUrl()
            }
            false -> {
                checkForWritePerm()
            }
        }
        binding?.apply {
            cl1.makeGone()
            cl2.makeVisible()
            pfDetect.pFrame.makeVisible()
            pfDetect.progressBarLayout.progressBar.makeVisible()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        when (event?.getString()) {
            Constants.EXECUTE_WRITE_PERM -> {
                execute()
            }
            Constants.EXECUTE_CAMERA_PERM -> {
                startCamera()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    private fun execute() {
        this.finalUri = uriFromBitmap()
        getStorageUrl()
    }

    private fun checkForWritePerm() {
      permissionHelper.checkForPerm(Constants.WRITE_PERM)
    }

    private fun uriFromBitmap(): Uri {
        val bytes = ByteArrayOutputStream()
        model.getBitmap()!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            model.getBitmap(),
            "Title",
            null
        )
        return Uri.parse(path)
    }


    private fun getStorageUrl() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                ref.putFile(finalUri).addOnCompleteListener(object :
                    OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                        if (p0.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                detectFaceThroughAzure(uri.toString())
                            }
                        }
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun toResultSongFragment() {
        findNavController().navigate(R.id.action_faceProceedOrRetakeFragment_to_resultSongsFragment)
    }

    private fun detectFace() {
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()

        when (boolean) {
            true -> {
                try {
                    testImage = InputImage.fromFilePath(requireContext(), imageUri!!.toUri())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            false -> {
                testImage = InputImage.fromBitmap(model.getBitmap()!!, 0)
            }
        }

        detect(testImage, highAccuracyOpts)

    }

    private fun detect(testImage: InputImage, highAccuracyOpts: FaceDetectorOptions) {
        binding?.apply {
            cl1.makeGone()
            cl2.makeVisible()
            pfDetect.pFrame.makeVisible()
            pfDetect.progressBarLayout.progressBar.makeVisible()
        }
        val detector = FaceDetection.getClient(highAccuracyOpts)

        val result = detector.process(testImage)
            .addOnSuccessListener { faces ->
                facesInfo(faces)
            }
            .addOnFailureListener { e ->
                binding?.apply {
                    cl1.makeVisible()
                    makeViewsGone()
                }
                showToast(e.message.toString())
            }
    }

    private fun makeViewsGone() {
        binding?.apply {
            cl2.makeGone()
            pfDetect.pFrame.makeGone()
            pfDetect.progressBarLayout.progressBar.makeGone()
        }
    }

    private fun facesInfo(faces: List<Face>) {
        if (faces.isEmpty()) {
            binding?.apply {
                makeViewsGone()
                cl4.makeVisible()
            }
        } else {
            binding?.apply {
                makeViewsGone()
                cl3.makeVisible()
            }
            analyzeFace(faces[0])
        }

    }

    private fun analyzeFace(face: Face) {
        val sProb = face.smilingProbability
        if (sProb != null) {
            if (sProb < 0.15.toFloat()) {
                setEmotion(R.drawable.sad_face, "Sad")
                this.mood = Constants.SAD_MOOD
            } else if (sProb in 0.15..0.7) {
                setEmotion(R.drawable.neutral_face, "Neutral")
                this.mood = Constants.NEUTRAL_MOOD
            } else if (sProb > 0.7 && sProb <= 1) {
                setEmotion(R.drawable.happy_face, "Happy")
                this.mood = Constants.HAPPY_MOOD
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setEmotion(i: Int, mood: String) {
        binding?.faceResultFragment?.detectedEmotion?.setImageDrawable(resources.getDrawable(i))
        binding?.faceResultFragment?.tvEmotion?.text = mood
    }


    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun btnCamera() {
        dismissDialog()
        checkForCamPerm()
    }

    private fun checkForCamPerm() {
        permissionHelper.checkForPerm(Constants.CAMERA_PERM)
    }

    override fun btnGallery() {
        dismissDialog()
        galleryCall()
    }

    private fun dismissDialog() {
        dialog.dismissDialog()
    }

    private fun showDialog() {
        dialog = Dialog(requireContext(), this)
        dialog.showDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            binding?.apply {
                ivFaceScan.setImageBitmap(bitmap)
                model.setBitmap(bitmap!!)
                boolean = false
            }
        }
    }

    private fun startCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE)
    }


}