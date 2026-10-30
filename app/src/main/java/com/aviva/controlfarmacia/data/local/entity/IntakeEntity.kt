package com.aviva.controlfarmacia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One recorded dose. Rows disappear with their medication. */
@Entity(
    tableName = "intakes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("takenAt")]
)
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    /** Epoch milliseconds. */
    val takenAt: Long,
    /** Reminder slot formatted as `HH:mm`, when the intake came from a reminder. */
    val scheduledTime: String? = null
)
