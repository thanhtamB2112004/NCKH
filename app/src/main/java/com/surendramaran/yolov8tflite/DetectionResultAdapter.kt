package com.surendramaran.yolov8tflite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8tflite.databinding.ItemDetectionResultBinding

class DetectionResultAdapter(private val results: List<DetectionResult>) :
    RecyclerView.Adapter<DetectionResultAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemDetectionResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetectionResult, position: Int) {
            binding.txtResults.text = item.clsName
            binding.txtResults2.text = "Bệnh: ${item.clsName}"
            binding.txtResults3.text = "Cách điều trị: \n${item.cdtText}\n"
            binding.txtResults4.text = "Cách phòng tránh: \n${item.cptText}\n"
            updateExpandableView(item.isExpanded)

            // Cập nhật trạng thái hiển thị khi bind dữ liệu


            binding.layoutExpandable.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(adapterPosition, item.isExpanded) // Cập nhật lại item khi bấm
            }
        }
        fun updateExpandableView(isExpanded: Boolean) {
            binding.txtResults2.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.txtResults3.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.txtResults4.visibility = if (isExpanded) View.VISIBLE else View.GONE

            binding.imgArrow.rotation = if (isExpanded) 180f else 0f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetectionResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        holder.bind(item, position)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val isExpanded = payloads[0] as Boolean
            holder.updateExpandableView(isExpanded)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = results.size
}
