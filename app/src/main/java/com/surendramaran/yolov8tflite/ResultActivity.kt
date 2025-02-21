package com.surendramaran.yolov8tflite

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.surendramaran.yolov8tflite.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu từ Intent
        val imagePath = intent.getStringExtra("image_path")
        val inferenceTime = intent.getLongExtra("inference_time", 0L)
        val boundingBoxes = intent.getParcelableArrayListExtra<BoundingBox>("bounding_boxes")

        // Hiển thị ảnh
        val bitmap = BitmapFactory.decodeFile(imagePath)
        binding.imageView.setImageBitmap(bitmap)

        // Hiển thị thời gian suy luận
        binding.inferenceTime.text = "Thời gian suy luận: ${inferenceTime}ms"

        // Hiển thị thông tin bounding boxes
        val resultText = boundingBoxes?.joinToString("\n") { box ->
            "Lớp: ${box.clsName}, Xác suất: ${"%.2f".format(box.cnf)}, Vị trí: (${box.rect.left}, ${box.rect.top}, ${box.rect.right}, ${box.rect.bottom})"
        } ?: "Không phát hiện đối tượng nào."

        binding.resultText.text = resultText
    }
}
