package com.surendramaran.yolov8tflite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ConTrungDAO {
    @Insert
    fun insertConTrung(insect: ConTrung): Long

    @Query("SELECT * FROM con_trung WHERE CT_Ten LIKE :name LIMIT 1")
    fun getRecommendationByInsect(name: String): ConTrung?

    @Query("SELECT * FROM con_trung")
    fun getAllConTrung(): List<ConTrung>
}
