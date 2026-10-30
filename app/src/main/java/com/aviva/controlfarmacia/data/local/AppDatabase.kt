package com.aviva.controlfarmacia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aviva.controlfarmacia.data.local.converters.Converters
import com.aviva.controlfarmacia.data.local.dao.MedicationDao
import com.aviva.controlfarmacia.data.local.entity.MedicationEntity

@Database(entities = [MedicationEntity::class], version = AppDatabase.VERSION, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        const val NAME = "control_farmacia_db"
        const val VERSION = 3
    }
}
