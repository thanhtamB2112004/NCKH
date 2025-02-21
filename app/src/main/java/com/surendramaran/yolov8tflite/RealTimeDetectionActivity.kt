package com.surendramaran.yolov8tflite

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import com.surendramaran.yolov8tflite.Detector.DetectorListener
import com.surendramaran.yolov8tflite.databinding.DetectLayoutBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

class RealTimeDetectionActivity : AppCompatActivity(), DetectorListener {
    private var binding: DetectLayoutBinding? = null
    private var detector: Detector? = null
    private var btnConfirm: Button? = null
    private var btnSelectImage: Button? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var selectedImagePath: String? = null
    private var detectedBoundingBoxes: List<BoundingBox>? = null
    private var currentFrameBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetectLayoutBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        btnConfirm = binding!!.btnConfirm

        detector = Detector(this, MODEL_PATH, LABELS_PATH, this)
        detector?.setup()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnConfirm!!.setOnClickListener { v: View? -> onConfirmDetection() }
    }

    private fun startCamera() {
        ProcessCameraProvider.getInstance(this).addListener({
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Camera initialization failed.",
                    e
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding!!.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }

            currentFrameBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            currentFrameBitmap?.let {
                detector?.detect(it)
            } ?: Log.e(TAG, "currentFrameBitmap is null!")

        }


        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            preview.setSurfaceProvider(binding!!.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }
    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding?.inferenceTime?.text = "${inferenceTime}ms"
            detectedBoundingBoxes = boundingBoxes
            binding?.overlay?.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }


    private fun drawBoundingBoxesOnBitmap(bitmap: Bitmap?, boundingBoxes: List<BoundingBox>): Bitmap? {
        if (bitmap == null) return null

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 40f
            style = Paint.Style.FILL
        }

        boundingBoxes.forEach { box ->
            paint.color = getRandomColor()
            textPaint.color = paint.color

            val left = box.x1 * canvas.width
            val top = box.y1 * canvas.height
            val right = box.x2 * canvas.width
            val bottom = box.y2 * canvas.height

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawText(box.clsName, left, top - 10, textPaint)

            Log.d("Real-Time Detection", "Bounding Box: $left, $top, $right, $bottom")
        }

        return mutableBitmap
    }
    fun getRandomColor(): Int {
        val r = Random.nextInt(0, 256)  // Màu đỏ từ 0 đến 255
        val g = Random.nextInt(0, 256)  // Màu xanh lá cây từ 0 đến 255
        val b = Random.nextInt(0, 256)  // Màu xanh dương từ 0 đến 255
        return Color.rgb(r, g, b)  // Trả về màu RGB
    }

    override fun onEmptyDetect() {
        runOnUiThread { binding?.overlay?.invalidate() }
    }


    private fun onConfirmDetection() {
        if (detectedBoundingBoxes.isNullOrEmpty()) {
            Toast.makeText(this, "No objects detected!", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmapWithBoxes = captureImageWithBoundingBox()
        if (bitmapWithBoxes == null) {
            Toast.makeText(this, "Error capturing image!", Toast.LENGTH_SHORT).show()
            return
        }

        val detectedLabels = detectedBoundingBoxes!!.map { it.clsName }
        val bottomSheet = ResultBottomSheetFragment.newInstance(ArrayList(detectedLabels), bitmapWithBoxes)
        bottomSheet.show(supportFragmentManager, "ResultBottomSheet")
    }

private fun captureImageWithBoundingBox(): Bitmap? {
    currentFrameBitmap?.let { originalBitmap ->
        return drawBoundingBoxesOnBitmap(originalBitmap, detectedBoundingBoxes ?: emptyList())
    }
    return null
}


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "Camera"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
