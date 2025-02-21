//package com.surendramaran.yolov8tflite
//
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Typeface
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.lifecycle.lifecycleScope
//import androidx.room.Room
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//import com.surendramaran.yolov8tflite.databinding.FragmentResultBottomSheetBinding
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class ResultBottomSheetFragment : BottomSheetDialogFragment() {
//    private var _binding: FragmentResultBottomSheetBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var btnClose: Button
//
//    private lateinit var db: AppDatabase
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentResultBottomSheetBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////
////        db = Room.databaseBuilder(
////            requireContext(),
////            AppDatabase::class.java,
////            "app_database"
////        ).fallbackToDestructiveMigration()
////            .build()
////        btnClose = view.findViewById(R.id.btnClose)
////        val detectionResults = arguments?.getStringArrayList("DETECTION_RESULTS")
////        val bitmap = arguments?.getParcelable<Bitmap>("DETECTION_IMAGE")
////
////        // Hiển thị ảnh detect
////        if (bitmap != null) {
////            binding.imgResult.setImageBitmap(bitmap)
////        } else {
////            binding.imgResult.setImageResource(android.R.drawable.stat_notify_error) // Ảnh mặc định nếu không có ảnh
////        }
////
////        if (detectionResults != null) {
////            lifecycleScope.launch(Dispatchers.IO) {
////                try {
////                    val CDT = db.cachDieuTri().getCachDieuTribyCT("raynau")
////                    val resultText = CDT.joinToString("\n")
////                    for (i in 1..10) {
////                        resultText + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
////                    }
////                    resultText + "alooooooooooooooooooooooooooodâddddddddddddddddddddddddddd"
////                    launch(Dispatchers.Main) {
////                        binding.txtResults.text = resultText
////                        binding.txtResults2.text = resultText
////                        binding.txtResults3.text = resultText
////                        binding.txtResults4.text = resultText
////                    }
////
////                    Log.d("ResultBottomSheet", "Kết quả detect:\n$resultText")
////                } catch (e: Exception) {
////                    Log.e("DatabaseError", "Failed to fetch data: ${e.localizedMessage}")
////                }
////            }
////        } else {
////            binding.txtResults.text = "Không có kết quả nhận diện"
////        }
////        btnClose.setOnClickListener {
////            dismiss() // Đóng BottomSheet
////        }
////        // Bắt sự kiện vuốt xuống để ẩn popup
////        binding.root.setOnClickListener {
////            dismiss()
////        }
////    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        db = Room.databaseBuilder(
//            requireContext(),
//            AppDatabase::class.java,
//            "app_database"
//        ).fallbackToDestructiveMigration()
//            .build()
//
//        btnClose = view.findViewById(R.id.btnClose)
//        val detectionResults = arguments?.getStringArrayList("DETECTION_RESULTS")
//        val bitmap = arguments?.getParcelable<Bitmap>("DETECTION_IMAGE")
//        val boundingBoxes = arguments?.getParcelableArrayList<BoundingBox>("DETECTION_BOXES")
//
//        if (bitmap != null) {
//            // Vẽ bounding boxes lên ảnh
//            val bitmapWithBoxes = drawBoundingBoxes(bitmap, boundingBoxes ?: emptyList())
//            binding.imgResult.setImageBitmap(bitmapWithBoxes)
//        } else {
//            binding.imgResult.setImageResource(android.R.drawable.stat_notify_error)
//        }
//
//        // Hiển thị thông tin phát hiện
//        if (detectionResults != null && detectionResults.isNotEmpty()) {
//            val resultText = detectionResults.joinToString("\n") { result ->
//                "- $result"
//            }
//            binding.txtResults.text = resultText
//        } else {
//            binding.txtResults.text = "Không có đối tượng nào được phát hiện."
//        }
//
//        btnClose.setOnClickListener {
//            dismiss() // Đóng BottomSheet
//        }
//
//        // Bắt sự kiện vuốt xuống để ẩn popup
//        binding.root.setOnClickListener {
//            dismiss()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    companion object {
//        fun newInstance(detectionResults: ArrayList<String>, bitmap: Bitmap?) =
//            ResultBottomSheetFragment().apply {
//                arguments = Bundle().apply {
//                    putStringArrayList("DETECTION_RESULTS", detectionResults)
//                    putParcelable("DETECTION_IMAGE", bitmap)
//                }
//            }
//    }
//
//    private fun drawBoundingBoxes(originalBitmap: Bitmap, boxes: List<BoundingBox>): Bitmap {
//        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(bitmap)
//        val paint = Paint().apply {
//            color = Color.RED
//            style = Paint.Style.STROKE
//            strokeWidth = 5f
//        }
//        val textPaint = Paint().apply {
//            color = Color.WHITE
//            textSize = 40f
//            typeface = Typeface.DEFAULT_BOLD
//        }
//
//        for (box in boxes) {
//            canvas.drawRect(box.rect, paint)
//            canvas.drawText("${box.clsName} (${String.format("%.2f", box.cnf)})",
//                box.rect.left, box.rect.top - 10, textPaint)
//        }
//
//        return bitmap
//    }
//
//}
package com.surendramaran.yolov8tflite

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.surendramaran.yolov8tflite.databinding.FragmentResultBottomSheetBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.animation.ValueAnimator
import androidx.core.animation.addListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.withContext

class ResultBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentResultBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var btnClose: Button
    var resultsList: List<DetectionResult>? = null


    private lateinit var db: AppDatabase
    // Biến trạng thái mở rộng hay thu gọn
    var isExpanded = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration()
            .build()
        btnClose = view.findViewById(R.id.btnClose)
        val detectionResults = arguments?.getStringArrayList("DETECTION_RESULTS")
        val bitmap = arguments?.getParcelable<Bitmap>("DETECTION_IMAGE")

        // Hiển thị ảnh detect
        if (bitmap != null) {
            binding.imgResult.setImageBitmap(bitmap)
        } else {
            binding.imgResult.setImageResource(android.R.drawable.stat_notify_error) // Ảnh mặc định nếu không có ảnh
        }
        var insect = arrayOf("Bọ trĩ", "Rầy nâu", "Sâu cuốn lá")
        var disease = arrayOf("Cháy bìa lá", "Đốm nâu", "Đạo ôn", "Bọ gai")

        if (detectionResults != null) {
//            detectionResults.add("Bọ gai")
            println(detectionResults.toString())
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val CDT: MutableList<CachDieuTri> = mutableListOf()
                    val CPT: MutableList<CachPhongTranh> = mutableListOf()
                    for (clsName in detectionResults) {


                        if (disease.contains(clsName)) {
                            println("Bệnh: $clsName")
                            CDT.addAll(db.cachDieuTri().getCachDieuTribyBenh(clsName))
                            CPT.addAll(db.cachPhongTranh().getCachPhongTranhbyBenh(clsName))
                        } else if (insect.contains(clsName)) {
                            println("Côn trùng: $clsName")
                            CDT.addAll(db.cachDieuTri().getCachDieuTribyCT(clsName))
                            println(db.cachDieuTri().getCachDieuTribyCT(clsName))
                            CPT.addAll(db.cachPhongTranh().getCachPhongTranhbyCT(clsName))
                            println(db.cachPhongTranh().getCachPhongTranhbyCT(clsName))
                        }


                        println("Cách điều trị: $CDT")
                        println("Cách phòng tránh: $CPT")
                    }


                    Log.d("RESULT", detectionResults.toString())

//                    val title = "Kết quả nhận diện"
//                    val clsName = "Bệnh: " + detectionResults[0]
//                    val cdt_text = "Cách điều trị: \n"+CDT[0].CDT_ChiTiet + "\n"
//                    val cpt_text = "Cách phòng tránh: \n"+CPT[0].CPT_ChiTiet + "\n"

                    resultsList = detectionResults.mapIndexed { index, cls ->
                        DetectionResult(
                            clsName = "$cls",
                            cdtText = "\n${CDT[index].CDT_ChiTiet}\n",
                            cptText = "\n${CPT[index].CPT_ChiTiet}\n"
                        )
                    }
                    withContext(Dispatchers.Main) {
                        val adapter = DetectionResultAdapter(resultsList!!.toMutableList()) // Chuyển sang MutableList
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//                        binding.recyclerView.setHasFixedSize(true)
//                        binding.recyclerView.itemAnimator = DefaultItemAnimator()
                        binding.recyclerView.adapter = adapter
                    }

//                    Log.d("ResultBottomSheet", "Kết quả detect:\n$resultText")
                } catch (e: Exception) {
                    Log.e("DatabaseError", "Failed to fetch data: ${e.localizedMessage}")
                }
            }
        } else {
            Log.e("DETECTION", "Không có kết quả nhận diện")
        }
        btnClose.setOnClickListener {
            dismiss() // Đóng BottomSheet
        }
        // Bắt sự kiện vuốt xuống để ẩn popup
        binding.root.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        fun newInstance(detectionResults: ArrayList<String>, bitmap: Bitmap?) =
            ResultBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList("DETECTION_RESULTS", detectionResults)
                    putParcelable("DETECTION_IMAGE", bitmap)
                }
            }
    }
}
