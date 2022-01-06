package com.example.emotionbasedmusic.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.emotionbasedmusic.R
import com.example.emotionbasedmusic.databinding.FragmentFaceProceedOrRetakeBinding
import com.example.emotionbasedmusic.databinding.FragmentMusicBinding
import com.example.emotionbasedmusic.helper.Constants
import com.example.emotionbasedmusic.helper.Dialog
import com.example.emotionbasedmusic.mlFace.FaceContourGraphic
import com.example.emotionbasedmusic.mlFace.GraphicOverlay
import com.example.emotionbasedmusic.viewModel.MusicViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException


class FaceProceedOrRetakeFragment : Fragment(), View.OnClickListener, Dialog.IListener {
    private lateinit var binding: FragmentFaceProceedOrRetakeBinding
    private val model: MusicViewModel by activityViewModels()
    private val navArgs: FaceProceedOrRetakeFragmentArgs by navArgs()
    private var imageUri: String? = null
    private var boolean: Boolean = false
    private var bitmap: Bitmap? = null
    private lateinit var mGraphicOverlay: GraphicOverlay
    private lateinit var dialog: Dialog
    private lateinit var image: InputImage
    private lateinit var img: FirebaseVisionImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = navArgs.uri
        boolean = navArgs.isFromGallery
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaceProceedOrRetakeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        binding.apply {
            if(boolean) {
                ivFaceScan.setImageURI(imageUri?.toUri())
            }
            else {
                ivFaceScan.setImageBitmap(model.getBitmap())
            }

            btnProceed.setOnClickListener(this@FaceProceedOrRetakeFragment)
            btnRetake.setOnClickListener(this@FaceProceedOrRetakeFragment)
        }
        mGraphicOverlay = binding.graphicOverlay
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent?  = result.data
            if(data!=null) {
                binding.ivFaceScan.setImageURI(imageUri?.toUri())
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
        when(p0?.id) {
            R.id.btnRetake -> {showDialog()}
            R.id.btnProceed -> {createFirebaseVisionImage()}
        }
    }

    private fun createFirebaseVisionImage() {
        when(boolean) {
            true -> {
                try {
                    img = FirebaseVisionImage.fromFilePath(requireContext(), imageUri!!.toUri())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            false -> {
                img = FirebaseVisionImage.fromBitmap(model.getBitmap()!!)
            }
        }
        val detector = FirebaseVision.getInstance().visionFaceDetector

        detector.detectInImage(img)
            .addOnSuccessListener { faces -> showResults(faces)}
            .addOnFailureListener { e->
                showToast(e.message.toString())
            }
    }

    private fun showResults(faces: List<FirebaseVisionFace>) {
        for(face in faces) {
            if(faces.isEmpty()) {
                val msg = "No Face Detected"
                showToast(msg)
            }
            else {
                val msg = "Smiling Probability: ${face.smilingProbability}"
                showToast(msg)
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun btnCamera() {
        dismissDialog()
        openCamera()
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

    private fun openCamera() {
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.CAMERA), Constants.CAMERA_PERMISSION_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            binding.apply {
                ivFaceScan.setImageBitmap(bitmap)
            }
        }
    }
    private fun startCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE)
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

    private fun runFaceContourDetection() {
        image = when(boolean) {
            true -> {
                InputImage.fromFilePath(requireContext(), imageUri?.toUri()!!)
            }
            false -> {
                InputImage.fromBitmap(model.getBitmap()!!, 0)
            }
        }
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        val detector: FaceDetector = FaceDetection.getClient(options)
        detector.process(image)
            .addOnSuccessListener { faces ->
                processFaceContourDetectionResult(faces as List<Face>)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
    }

    private fun processFaceContourDetectionResult(faces: List<Face>) {
        // Task completed successfully
        if (faces.size == 0) {
//            showToast("No face found")
            return
        }
        mGraphicOverlay.clear()
        for (i in faces.indices) {
            val face: Face = faces[i]
            val faceGraphic = FaceContourGraphic(mGraphicOverlay)
            mGraphicOverlay.add(faceGraphic)
            faceGraphic.updateFace(face)
        }
    }



}