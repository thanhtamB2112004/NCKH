package com.surendramaran.yolov8tflite

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "con_trung")
data class ConTrung(
    @PrimaryKey val CT_ID: String,
    val CT_Ten: String,
    val CT_BieuHien: String,
)

