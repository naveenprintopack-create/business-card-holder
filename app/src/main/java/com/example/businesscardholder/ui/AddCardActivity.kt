package com.example.businesscardholder.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.example.businesscardholder.data.AppDatabase
import com.example.businesscardholder.data.BusinessCard
import com.example.businesscardholder.databinding.ActivityAddCardBinding
import com.example.businesscardholder.ocr.OcrHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCardBinding
    private var imageCapture: ImageCapture? = null
    private var capturedImagePath: String = ""
    private val dao by lazy { AppDatabase.getInstance(this).businessCardDao() }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else {
            Toast.makeText(this, "Camera permission is needed to scan cards", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCapture.setOnClickListener { capturePhoto() }
        binding.btnRetake.setOnClickListener { showCameraView() }
        binding.btnSave.setOnClickListener { saveCard() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
                Toast.makeText(this, "Could not start camera", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val capture = imageCapture ?: return

        val imagesDir = File(filesDir, "card_images").apply { mkdirs() }
        val fileName = "card_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val outputFile = File(imagesDir, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        binding.btnCapture.isEnabled = false

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImagePath = outputFile.absolutePath
                    binding.btnCapture.isEnabled = true
                    onPhotoReady(outputFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exc)
                    binding.btnCapture.isEnabled = true
                    Toast.makeText(this@AddCardActivity, "Capture failed, try again", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun onPhotoReady(file: File) {
        showFormView()
        binding.ivCapturedPhoto.setImageResource(0)

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) { loadRotatedBitmap(file) }
            binding.ivCapturedPhoto.setImageBitmap(bitmap)

            Toast.makeText(this@AddCardActivity, "Reading text from photo…", Toast.LENGTH_SHORT).show()
            try {
                val rawText = OcrHelper.recognizeText(bitmap)
                val parsed = OcrHelper.parse(rawText)
                binding.etName.setText(parsed.contactName)
                binding.etCompany.setText(parsed.companyName)
                binding.etJobTitle.setText(parsed.jobTitle)
                binding.etPhone.setText(parsed.phoneNumber)
                binding.etEmail.setText(parsed.email)
                binding.etWebsite.setText(parsed.website)
                binding.etAddress.setText(parsed.address)
            } catch (e: Exception) {
                Log.e(TAG, "OCR failed", e)
                Toast.makeText(
                    this@AddCardActivity,
                    "Couldn't read text automatically — please fill in the details manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Reads EXIF orientation and rotates the bitmap so it displays upright. */
    private fun loadRotatedBitmap(file: File): Bitmap {
        val original = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        if (rotationDegrees == 0) return original
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(
            original, 0, 0, original.width, original.height, matrix, true
        )
        // Persist the corrected orientation back to disk so the thumbnail in the list looks right too.
        FileOutputStream(file).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return rotated
    }

    private fun saveCard() {
        val card = BusinessCard(
            contactName = binding.etName.text.toString().trim(),
            companyName = binding.etCompany.text.toString().trim(),
            jobTitle = binding.etJobTitle.text.toString().trim(),
            phoneNumber = binding.etPhone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            website = binding.etWebsite.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            notes = binding.etNotes.text.toString().trim(),
            imagePath = capturedImagePath
        )

        if (card.contactName.isBlank() && card.companyName.isBlank() && card.phoneNumber.isBlank()) {
            Toast.makeText(this, "Please fill in at least a name, company, or phone number", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            dao.insert(card)
            Toast.makeText(this@AddCardActivity, "Card saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showFormView() {
        binding.cameraContainer.visibility = View.GONE
        binding.formContainer.visibility = View.VISIBLE
    }

    private fun showCameraView() {
        binding.formContainer.visibility = View.GONE
        binding.cameraContainer.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "AddCardActivity"
    }
}
