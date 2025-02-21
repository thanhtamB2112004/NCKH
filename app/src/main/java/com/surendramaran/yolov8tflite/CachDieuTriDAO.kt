package com.surendramaran.yolov8tflite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CachDieuTriDAO {
    @Insert
    fun insertCDT(solution: CachDieuTri): Long

    @Query("SELECT * FROM cach_dieu_tri")
    fun getAllCachDieuTri(): List<CachDieuTri>

    @Query("""
        SELECT * FROM cach_dieu_tri cdt 
        LEFT JOIN benh b ON cdt.Benh_ID = b.Benh_ID
        WHERE B.Benh_Ten LIKE :benh_name
    """)
    fun getCachDieuTribyBenh(benh_name: String): List<CachDieuTri>

    @Query("""
        SELECT * FROM cach_dieu_tri cdt
        JOIN con_trung ct ON cdt.CT_ID = ct.CT_ID
        WHERE ct.CT_Ten LIKE :conTrung_name
    """)
    fun getCachDieuTribyCT(conTrung_name: String): List<CachDieuTri>
}
