//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.ms.square.android.expandabletextview.ExpandableTextView
//import com.surendramaran.yolov8tflite.R
//
//class ExpandableTextAdapter(private val dataList: List<String>) :
//    RecyclerView.Adapter<ExpandableTextAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val expandableTextView: ExpandableTextView = itemView.findViewById(R.id.expandable_text_view)
//        val textContent: TextView = itemView.findViewById(R.id.text_content)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_expandable_text, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val text = dataList[position]
//        holder.textContent.text = text
//
//        // Xử lý mở rộng khi nhấn vào
//        holder.textContent.setOnClickListener {
//            holder.expandableTextView.toggle()  // Mở rộng hoặc thu gọn
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return dataList.size
//    }
//}
