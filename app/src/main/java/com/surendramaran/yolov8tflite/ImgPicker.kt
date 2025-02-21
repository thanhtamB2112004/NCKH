package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.Detector.DetectorListener
import com.surendramaran.yolov8tflite.databinding.DetectLayoutBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

class ImgPicker : AppCompatActivity(), DetectorListener {
    private var detector: Detector? = null
    private var originalImg: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detector = Detector(this, MODEL_PATH, LABELS_PATH, this)
        detector!!.setup()
        selectImageFromGallery()

    }
    private val imagePickerLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val imageUri = result.data!!.data
            if (imageUri != null) processSelectedImage(imageUri)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    fun getRandomColor(): Int {
        val r = Random.nextInt(0, 256)  // Màu đỏ từ 0 đến 255
        val g = Random.nextInt(0, 256)  // Màu xanh lá cây từ 0 đến 255
        val b = Random.nextInt(0, 256)  // Màu xanh dương từ 0 đến 255
        return Color.rgb(r, g, b)  // Trả về màu RGB
    }

    private fun processSelectedImage(imageUri: Uri) {
        try {
            contentResolver.openInputStream(imageUri).use { inputStream ->
                originalImg = BitmapFactory.decodeStream(inputStream)
                if (originalImg != null) {
                    detector!!.detect(originalImg!!)

                } else {
                    Toast.makeText(this, "Không thể xử lý ảnh", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi xử lý ảnh", e)
        }
    }

    private fun drawBoundingBoxesOnBitmap(bitmap: Bitmap?, boundingBoxes: List<BoundingBox>): Bitmap? {
        if (bitmap == null) return null

        // Tạo một bản sao của bitmap để vẽ lên đó
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        val textPaint = Paint().apply {
            color = Color.RED  // Màu chữ mặc định có thể thay đổi sau
            textSize = 40f
            style = Paint.Style.FILL
        }
        // Vẽ các bounding boxes lên bitmap

        boundingBoxes.forEach { box ->
            paint.color = getRandomColor()
            textPaint.color = paint.color

            val left = box.x1 * canvas.width
            val top = box.y1 * canvas.height
            val right = box.x2 * canvas.width
            val bottom = box.y2 *canvas.height
            canvas.drawRect(left, top, right, bottom, paint)
            val className = box.clsName
            canvas.drawText(className, left, top - 10, textPaint)
            Log.d("Image Processed", (left+top+right+bottom).toString())
        }

        return mutableBitmap
    }

    override fun onEmptyDetect() {
        TODO("Not yet implemented")
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {

        val bitmapWithBoxes = drawBoundingBoxesOnBitmap(originalImg, boundingBoxes)
        if (bitmapWithBoxes==null)
        {
            Log.d("hinh` loi", "aloooo")
        }
        if (boundingBoxes.isNotEmpty()) {
            val results = ArrayList<String>()
            boundingBoxes.forEach { box ->
                val resultText = "Class: ${box.clsName}, Confidence: ${box.cnf}"
                results.add(box.clsName)
                Log.d("YOLOv8 Detection", resultText)
                Log.d("YOLOv8 Detection", String.format("Kết quả: %s", box.toString()))
//                Log.d("YOLOv8 Detection", box.toString())
            }

            // Chuyển sang ResultActivity với Handler để đảm bảo UI Thread không bị ảnh hưởng
            Handler(Looper.getMainLooper()).post {
                Log.d("YOLOv8 Detection", "Chuẩn bị mở ResultActivity với ${results.size} kết quả.")
                val resultBottomSheet = ResultBottomSheetFragment.newInstance(results, bitmapWithBoxes)
                Log.d("Classsssssss", results.toString())
                resultBottomSheet.show(supportFragmentManager, "ResultBottomSheet")
            }
        } else {
            Log.d("YOLOv8 Detection", "Không phát hiện đối tượng nào.")
        }
    }
}