package com.aviva.controlfarmacia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a medication. Column names are part of the on-device schema (version 3):
 * change them only together with a migration.
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Legacy column: the UI never let the user set it. Kept only to preserve the schema. */
    val dosageForm: String = "",
    val dosageValue: String? = null,
    /** Storage key of [com.aviva.controlfarmacia.domain.model.DosageUnit]. */
    val dosageUnit: String? = null,
    /** Name of [com.aviva.controlfarmacia.domain.model.PatientCategory]. */
    val patientCategory: String = "HUMAN",
    val patientName: String? = null,
    val isCurrentlyInTreatment: Boolean = false,
    /** Times formatted as `HH:mm`. */
    val reminderTimes: List<String> = emptyList(),
    val photoPath: String?,
    val expiryMonth: Int,
    val expiryYear: Int,
    val createdAt: Long = System.currentTimeMillis()
)
