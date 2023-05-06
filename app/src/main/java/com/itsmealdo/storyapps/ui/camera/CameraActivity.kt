package com.itsmealdo.storyapps.ui.camera

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.itsmealdo.storyapps.R
import com.itsmealdo.storyapps.databinding.ActivityCameraBinding
import com.itsmealdo.storyapps.ui.post.PostActivity
import com.itsmealdo.storyapps.utils.createFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private val binding: ActivityCameraBinding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    private var imgCapture: ImageCapture? = null
    private var camSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var tokenPhoto = false

    private lateinit var camExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        camExecutor = Executors.newSingleThreadExecutor()
        buttonSetup()
    }

    private fun buttonSetup() {
        binding.btnCamera.setOnClickListener {
            if (tokenPhoto) {
                Toast.makeText(this, getString(R.string.processing_capture), Toast.LENGTH_SHORT).show()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                tokenPhoto = true
                takePhotos()
            }
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnRotate.setOnClickListener {
            camSelector = if (camSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = ACTION_GET_CONTENT
        val chooser = Intent.createChooser(intent, "Choose a picture")
        intentGallery.launch(chooser)
    }

    private val intentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImage: Uri = it.data?.data as Uri
            val myFile = uriToFile(selectedImage, this@CameraActivity)
            val intent = Intent(this@CameraActivity, PostActivity::class.java)
            intent.putExtra(PostActivity.PHOTOS_RESULT_EXTRA, myFile)
            startActivity(intent)
            finish()
        }
    }

    private fun uriToFile(selectedImage: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = com.itsmealdo.storyapps.utils.createTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImage) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun takePhotos() {
        val photoFile = createFile(application)
        val imgCapture = imgCapture ?: return

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imgCapture.takePicture(outputOptions, camExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val intent = Intent(this@CameraActivity, PostActivity::class.java)
                intent.putExtra(PostActivity.PHOTOS_RESULT_EXTRA, photoFile)
                startActivity(intent)
                finish()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraActivity, getString(R.string.capture_failed), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        camExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        hideUISystem()
        startCamera()
    }

    private fun hideUISystem() {
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imgCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, camSelector, preview, imgCapture)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.camera_fail_open), Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }


}