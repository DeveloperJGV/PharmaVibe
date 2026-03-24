package com.aviva.controlfarmacia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosageForm: String, // e.g., Tablet, Syrup
    val photoPath: String?,
    val expiryMonth: Int,
    val expiryYear: Int,
    val createdAt: Long = System.currentTimeMillis()
)
