package com.surendramaran.yolov8tflite

import androidx.room.*

@Dao
interface CachPhongTranhDAO {
    @Insert
    fun insertCPT(prevention : CachPhongTranh): Long

    @Query("SELECT * FROM cach_phong_tranh")
    fun getAllCachPhongTranh(): List<CachPhongTranh>

    @Query("SELECT * FROM cach_phong_tranh c JOIN benh b " +
            "ON c.Benh_ID=b.Benh_ID " +
            "WHERE Benh_Ten LIKE :benh_name")
    fun getCachPhongTranhbyBenh(benh_name: String): List<CachPhongTranh>

    @Query("SELECT * FROM cach_phong_tranh c JOIN CON_TRUNG ct " +
            "ON c.CT_ID=ct.CT_ID " +
            "where CT_Ten LIKE :CT_name")
    fun getCachPhongTranhbyCT(CT_name: String) : List<CachPhongTranh>
}