package com.surendramaran.yolov8tflite

data class DetectionResult(
    val clsName: String,
    val cdtText: String,
    val cptText: String,
    var isExpanded: Boolean = false
)
