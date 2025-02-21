package com.surendramaran.yolov8tflite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BenhDAO {
    @Insert
    fun insertBenh(disease: Benh): Long

    @Query("SELECT * FROM benh WHERE Benh_Ten LIKE :name LIMIT 1")
    fun getRecommendationByDisease(name: String): Benh?

    @Query("SELECT * FROM benh")
    fun getAllBenh():List<Benh>
}
