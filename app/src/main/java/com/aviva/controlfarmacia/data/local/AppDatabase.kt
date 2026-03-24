package com.aviva.controlfarmacia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aviva.controlfarmacia.data.local.dao.MedicationDao
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity

@Database(entities = [MedicationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
