package com.surendramaran.yolov8tflite

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "benh")
data class Benh(
    @PrimaryKey val Benh_ID: String,
    val Benh_Ten: String,
    val Benh_NguyenNhan: String,
    val Benh_BieuHien: String,
)
