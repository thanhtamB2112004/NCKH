package com.surendramaran.yolov8tflite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Benh::class, ConTrung::class, CachDieuTri::class, CachPhongTranh::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun benhDao(): BenhDAO
    abstract fun conTrungDao(): ConTrungDAO
    abstract fun cachDieuTri(): CachDieuTriDAO
    abstract fun cachPhongTranh(): CachPhongTranhDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )

                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }
}
