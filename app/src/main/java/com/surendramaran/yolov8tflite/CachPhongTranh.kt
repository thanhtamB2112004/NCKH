package com.surendramaran.yolov8tflite

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "cach_phong_tranh",
    foreignKeys = [
        ForeignKey(
            entity = Benh::class,
            parentColumns = ["Benh_ID"],
            childColumns = ["Benh_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConTrung::class,
            parentColumns = ["CT_ID"],
            childColumns = ["CT_ID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class CachPhongTranh(
    @PrimaryKey(autoGenerate = true) val CPT_ID: Int = 0,
    val CPT_ChiTiet: String,
    val Benh_ID: String?,
    val CT_ID: String?,
)
