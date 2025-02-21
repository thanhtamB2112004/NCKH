package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import com.surendramaran.yolov8tflite.database.AppDatabase
//import com.surendramaran.yolov8tflite.database.entities.Benh
//import com.surendramaran.yolov8tflite.database.entities.Contrung
import android.widget.Toast
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationsActivity : AppCompatActivity() {

    private lateinit var recommendationTextView: TextView

    class RecommendationsActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_recommendations)

            val classNames = intent.getStringArrayListExtra("CLASS_NAMES")

            classNames?.let { names ->
                for (className in names) {
                    queryDatabaseForDetection(className)
                }
            } ?: run {
                Toast.makeText(this, "Không có dữ liệu lớp phát hiện!", Toast.LENGTH_SHORT).show()
            }
        }

        private fun queryDatabaseForDetection(className: String) {
            GlobalScope.launch(Dispatchers.Main) {
                val database = AppDatabase.getInstance(applicationContext)

                // Query for Disease
                val diseaseResult = withContext(Dispatchers.IO) {
                    database.benhDao().getRecommendationByDisease(className)
                }
                Log.i("Class Name","Class: ${className}")

                if (diseaseResult != null) {
                    displayResult(
                        "Bệnh",
                        diseaseResult.Benh_Ten,
                        diseaseResult.Benh_BieuHien,
                        diseaseResult.Benh_BieuHien,

                    )
                    return@launch
                }

                // Query for Insect
                val insectResult = withContext(Dispatchers.IO) {
                    database.conTrungDao().getRecommendationByInsect(className)
                }

                if (insectResult != null) {
                    displayResult(
                        "Côn trùng",
                        insectResult.CT_Ten,
                        insectResult.CT_BieuHien,
                        insectResult.CT_ID
                    )
                    return@launch
                }

                // If no data is found
                Toast.makeText(
                    this@RecommendationsActivity,
                    "Không tìm thấy thông tin cho lớp: $className",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun displayResult(
            type: String,
            name: String,
            description: String,
            solution: String
        ) {
            val message = """
            Loại: $type
            Tên: $name
            Mô tả: $description
            Giải pháp: $solution
        """.trimIndent()

            Log.d("DatabaseResult", message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

}

