package com.surendramaran.yolov8tflite

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.surendramaran.yolov8tflite.Detector.DetectorListener
import com.surendramaran.yolov8tflite.Constants.LABELS_PATH
import com.surendramaran.yolov8tflite.Constants.MODEL_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity(), DetectorListener {

    private lateinit var db: AppDatabase
    private var detector: Detector? = null
    private var originalImg: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration()
            .build()

        val fab =
            findViewById<Button>(R.id.btn_real_time_detect)
        val img_sec =
            findViewById<Button>(R.id.btn_select_image)

        fab.setOnClickListener { view: View? ->
            val intent =
                Intent(
                    this@MainActivity,
                    RealTimeDetectionActivity::class.java
                )
            startActivity(intent)
        }
        detector = Detector(this, MODEL_PATH, LABELS_PATH, this)
        detector!!.setup()
//        selectImageFromGallery()
        img_sec.setOnClickListener { view: View? ->
            selectImageFromGallery()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Insert dữ liệu mẫu về bệnh và côn trùng
        insertSampleData()

        observeData()
        Log.w("TAG", "TAG" )
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
        Log.d("DETECTION", "Empty Dectection")
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

    private fun insertSampleData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.benhDao().insertBenh(
                    Benh(
                        Benh_ID = "daoon",
                        Benh_Ten = "Đạo ôn",
                        Benh_NguyenNhan = "Bệnh đạo ôn do nấm Pyricularia oryzae gây ra. Bệnh phát sinh mạnh trong điều kiện thời tiết âm u, sáng sớm có sương mù hoặc mưa phùn. Nhiệt độ thích hợp từ 18 đến 26°C, ẩm độ cao (trên 90%).",
                        Benh_BieuHien = "Trên lá lúa: Vết bệnh hình thoi, phần giữa màu xám tro, viền nâu đậm. Khi bệnh nặng, các vết nối tiếp nhau làm lá bị cháy. Trên thân: Vết bệnh bao quanh đốt thân làm đốt khô, teo lại. Trên cổ bông: Vết bệnh màu đen làm cổ bông héo, bông lúa trắng hoặc lép lửng. Trên hạt: Vết bệnh màu nâu xám, không định hình.",
                        )
                )
                db.benhDao().insertBenh(
                    Benh(
                        Benh_ID = "chaybiala",
                        Benh_Ten = "Cháy bìa lá",
                        Benh_NguyenNhan = "Bệnh cháy bìa lá do vi khuẩn Xanthomonas oryzae gây ra. Bệnh phát triển trong điều kiện mưa nhiều, gió mạnh, gieo sạ dày, thừa đạm.",
                        Benh_BieuHien = "Vết bệnh là đốm úng nước ở rìa lá, sau chuyển vàng nhạt. Ranh giới giữa phần bệnh và không bệnh có dạng sóng.",
                        )
                )
                db.benhDao().insertBenh(
                    Benh(
                        Benh_ID = "domnau",
                        Benh_Ten = "Đốm nâu",
                        Benh_NguyenNhan = "Bệnh đốm nâu do nấm Helminthosporium oryzae và Curvularia lunata gây ra.",
                        Benh_BieuHien = "Vết bệnh ban đầu là chấm nhỏ nâu nhạt, phát triển thành vết nâu đậm hơn. Trên giống kháng, vết bệnh hình bầu dục nhỏ.",
                        )
                )
                db.benhDao().insertBenh(
                    Benh(
                        Benh_ID = "bogai",
                        Benh_Ten = "Bọ gai",
                        Benh_NguyenNhan = "Bệnh bọ gai do côn trùng bọ gai trưởng thành và ấu trùng gây ra.",
                        Benh_BieuHien = "Lá lúa có các dải trắng song song, gân lá bị tổn thương dẫn đến các vết trắng lớn.",
                        )
                )
                db.conTrungDao().insertConTrung(
                    ConTrung(
                        CT_ID = "raynau",
                        CT_Ten = "Rầy nâu",
                        CT_BieuHien = "Rầy nâu là loài có kích thước nhỏ, cả ấu trùng và thành trùng đều sống ở gốc lúa trên mặt nước, gây khó khăn trong việc phát hiện và tiêu diệt. Rầy nâu hút nhựa làm lá lúa khô héo",
                        )
                )
                db.conTrungDao().insertConTrung(
                    ConTrung(
                        CT_ID = "saucuonla",
                        CT_Ten = "Sâu cuốn lá",
                        CT_BieuHien = "Sâu cuốn lá gây hại cho lúa bằng cách cuốn lá lúa thành tổ và phá hại từ bên trong. Mỗi con sâu có thể phá hại từ 5-10 lá.",
                        )
                )
                db.conTrungDao().insertConTrung(
                    ConTrung(
                        CT_ID = "botri",
                        CT_Ten = "Bọ trĩ",
                        CT_BieuHien = "Bọ trĩ (Stenchaetothrips biformis) gây hại cho nhiều loại cây như cây ăn quả, hoa màu, và đặc biệt nguy hiểm với lúa. Bọ trĩ tấn công cây lúa từ khi mới mọc đến khi đẻ nhánh, làm lá lúa bị quăn lại, chuyển vàng và khô héo."
                        )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Dọn sạch tàn dư rơm rạ, không gieo sạ quá dày để ruộng thông thoáng, giảm độ ẩm. Bón phân cân đối, ưu tiên phân hữu cơ, tránh bón thừa đạm để tăng sức đề kháng cho cây. " +
                                "Quản lý nước hợp lý, không để ruộng quá khô hoặc ngập úng lâu ngày. Thường xuyên kiểm tra đồng ruộng để phát hiện sớm dấu hiệu bệnh. Khi bệnh xuất hiện, sử dụng thuốc đặc trị theo hướng dẫn để kiểm soát kịp thời.",
                        CPT_ID = 1,
                        Benh_ID = "daoon",
                        CT_ID = null
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Trong bón phân cần lưu ý, tránh bón quá nhiều đạm (N), vì điều này có thể làm cây lúa mềm yếu, dễ nhiễm bệnh. Ngoài ra, cần duy trì mực nước hợp lý trong ruộng, tránh để ruộng quá khô hoặc quá ẩm kéo dài. " +
                                "Bên cạnh đó, bà con nên thường xuyên kiểm tra đồng ruộng để phát hiện sớm và loại bỏ những cây bị bệnh, tránh lây lan. Nếu bệnh xuất hiện nhiều, có thể sử dụng thuốc bảo vệ thực vật chứa hoạt chất như Kasugamycin, Streptomycin hoặc Oxytetracycline theo khuyến cáo để kiểm soát bệnh hiệu quả.",
                        CPT_ID = 2,
                        Benh_ID = "chaybiala",
                        CT_ID = null
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Làm sạch cỏ dại trong ruộng và bờ bao. Diệt trừ sâu non trên mạ mùa sắp cấy bằng cách ngắt bỏ các lá bị hại. Áp dụng khoảng cách cấy mạ hẹp hơn với mật độ lá phủ dày hơn có thể chịu đựng được số lượng bọ nhiều hơn. Tránh bón phân đạm quá nhiều ở những cánh đồng bị nhiễm bọ.",
                        CPT_ID = 3,
                        Benh_ID = "bogai",
                        CT_ID = null
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Hạn chế ngộ độc hữu cơ bằng cách làm đất kỹ, bón lót vôi, phân lân, phân hữu cơ con bò sữa Không được để cây lúa thiếu đạm, thiếu hụt chất dinh dưỡng hỗ trợ sự sinh trưởng và phát triển của cây lúa. Cung cấp đủ nước cho vùng khô hạn, tránh đất bị nhiễm phèn, đặc biệt là vụ mùa Hè – Thu.",
                        CPT_ID = 4,
                        Benh_ID = "domnau",
                        CT_ID = null
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Phòng ngừa rầy nâu bằng cách gieo sạ tập trung theo khuyến cáo lịch thời vụ, vệ sinh đồng ruộng, cày trục kỹ trước khi gieo sạ, và sử dụng giống lúa kháng rầy.",
                        CPT_ID = 5,
                        Benh_ID = null,
                        CT_ID = "raynau"
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Phòng ngừa sâu cuốn lá bằng cách áp dụng biện pháp quản lý dịch hại tổng hợp (IPM), làm sạch cỏ dại quanh bờ ruộng, bón phân cân đối và hợp lý, đặc biệt là bón phân đạm vừa phải.",
                        CPT_ID = 6,
                        Benh_ID = null,
                        CT_ID = "saucuonla"
                    )
                )
                db.cachPhongTranh().insertCPT(
                    CachPhongTranh(
                        CPT_ChiTiet = "Phòng ngừa bọ trĩ bằng cách chọn giống lúa chịu sâu bệnh, chuẩn bị đất kỹ trước khi trồng, và chăm sóc tốt trong quá trình phát triển. " +
                                "Theo dõi tình hình phát triển của bọ trĩ và phun thuốc định kỳ khi cần thiết.",
                        CPT_ID = 7,
                        Benh_ID = null,
                        CT_ID = "botri"
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 1,
                        CDT_ChiTiet = "Để điều trị bệnh đạo ôn trên lúa, cần bón phân cân đối giữa các yếu tố N-P-K, ưu tiên sử dụng phân chuồng và phân hữu cơ để tăng cường sức đề kháng cho cây. Khi lúa trỗ bông, đảm bảo bộ lá đòng có màu xanh hơi vàng giúp hạn chế bệnh đạo ôn phát triển. " +
                                "Bên cạnh đó, tích cực kiểm tra đồng ruộng và theo dõi tình hình thời tiết, đặc biệt trong những giai đoạn thời tiết ẩm ướt. Để phòng ngừa bệnh đạo ôn cổ bông, phun thuốc 2 lần: lần 1 khi lúa thấp thoi trỗ, lần 2 khi lúa trỗ hoàn toàn. Các loại thuốc có thể sử dụng bao gồm FILIA 525SE, BEAM 75 WP, FUARMY 40EC, giúp kiểm soát hiệu quả bệnh đạo ôn.",
                        Benh_ID = "daoon",
                        CT_ID = null
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 2,
                        CDT_ChiTiet = "Để trị bệnh cháy bìa lá trên lúa, cần cắt tỉa các lá bị bệnh, bón phân hợp lý, đặc biệt là Kali, tránh bón quá nhiều đạm. Duy trì mực nước ruộng ổn định, không để quá khô hoặc quá ẩm. Xử dụng các loại thuốc như Forliet 80WP, Kasumin, Ketomium để phun để điều trị bệnh. " +
                                "Đồng thời, kiểm tra đồng ruộng thường xuyên để phát hiện sớm và xử lý kịp thời.",
                        Benh_ID = "chaybiala",
                        CT_ID = null
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 3,
                        CDT_ChiTiet = "Làm sạch cỏ dại trong ruộng và bờ bao. Diệt trừ sâu non trên mạ mùa sắp cấy bằng cách ngắt bỏ các lá bị hại có bọ gai. Dùng thuốc trừ sâu nhóm lân hữu cơ, Carbamate hoặc Cúc tổng hợp đều có thể diệt được bọ gai trưởng thành và ấu trùng. Các loại thuốc thường dùng bao gồm: Dursban 20EC, Regent 5SC, Confidor 17.8SL.",
                        Benh_ID = "bogai",
                        CT_ID = null
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 4,
                        CDT_ChiTiet = "Để điều trị bệnh đốm nâu trên lá lúa, trước hết, cần làm sạch cỏ dại trong ruộng và bờ bao để giảm nguy cơ lây lan mầm bệnh. Diệt trừ sâu non trên mạ mùa sắp cấy bằng cách ngắt bỏ các lá bị hại có bọ gai. Sử dụng thuốc trừ sâu thuộc nhóm lân hữu cơ, Carbamate hoặc Cúc tổng hợp để diệt bọ gai trưởng thành và ấu trùng, từ đó ngăn ngừa sự phát triển của bệnh. " +
                                "Các biện pháp này giúp bảo vệ lá lúa khỏi tác động của bệnh đốm nâu, đồng thời tăng cường sức đề kháng cho cây. Các loại thuốc thường dùng cho loại bệnh này là Tilt Super 300EC, Carbenzim 500FL, AmistarTop 325SC.",
                        Benh_ID = "domnau",
                        CT_ID = null
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 5,
                        CDT_ChiTiet = "Khi phát hiện mật số rầy nâu cao (3 con/tép trở lên), tiến hành phun thuốc trừ rầy theo nguyên tắc '4 đúng': Đúng loại thuốc, đúng liều lượng, đúng lúc, và đúng cách. Các loại thuốc sử dụng: Applaud, Bassa, Chess.",
                        Benh_ID = null,
                        CT_ID = "raynau"
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 6,
                        CDT_ChiTiet = "Sử dụng các loại thuốc có hoạt chất như Indoxacarb, Chlorantraniliprole, Flubendiamide, và Lufenuron. Các loại thuốc phổ biến bao gồm: Clever 150SC, Virtako 40 WG, Prevanthon 5SC, Minecto Star 60 WG, Viliam Targo 063 SC, Takumi 20WG, Nativo 750 WG, Lupenron 050SC.",
                        Benh_ID = null,
                        CT_ID = "saucuonla"
                    )
                )
                db.cachDieuTri().insertCDT(
                    CachDieuTri(
                        CDT_ID = 7,
                        CDT_ChiTiet = "Sử dụng các loại thuốc trừ sâu như IMNADA 100WP và BPDYGAN 5.4 EC để diệt trừ bọ trĩ. IMNADA 100WP có khả năng thẩm thấu mạnh và bảo vệ cây trong 3-4 tuần. BPDYGAN 5.4 EC có tác dụng làm gián đoạn hệ thần kinh của bọ trĩ, hiệu lực kéo dài.",
                        Benh_ID = null,
                        CT_ID = "botri"
                    )
                )
            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to insert sample data: ${e.localizedMessage}")
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch(Dispatchers.IO) {  // Specify Dispatchers.IO for background thread
            try {
                // Fetch all Benh entries
                val benhs = db.benhDao().getAllBenh()
                benhs.forEach { benh ->
                    Log.i("BenhInfo", "Benh: ${benh.Benh_Ten}, Nguyen nhan: ${benh.Benh_NguyenNhan}, Bieu hien: ${benh.Benh_BieuHien}")
                }

                // Fetch all ConTrung entries
                val conTrungs = db.conTrungDao().getAllConTrung()
                conTrungs.forEach { conTrung ->
                    Log.i("ConTrungInfo", "Con trung: ${conTrung.CT_Ten}, Bieu Hien: ${conTrung.CT_BieuHien}")
                }

                val CDT = db.cachDieuTri().getCachDieuTribyCT("Rầy nâu")
                val CPT = db.cachPhongTranh().getCachPhongTranhbyBenh("Đạo ôn")

                Log.i("CachDieuTriInfo", "$CDT")
                Log.i("CachPhongTranhInfo", "$CPT")

            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to fetch data: ${e.localizedMessage}")
            }
        }
    }


}
