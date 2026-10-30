package com.aviva.controlfarmacia.domain.model

import java.time.LocalTime
import java.time.YearMonth

/** Who the medication is for. */
enum class PatientCategory { HUMAN, PET }

/** Dosage units offered in the registration form. [storageKey] is what Room stores. */
enum class DosageUnit(val storageKey: String) {
    MILLIGRAM("mg"),
    MILLILITER("ml"),
    INTERNATIONAL_UNIT("UI"),
    GRAM("g"),
    CAPSULE("capsules"),
    TABLET("tablets"),
    DROP("drops"),
    PUFF("puffs"),
    PIECE("pieces");

    companion object {
        fun fromStorageKey(key: String?): DosageUnit? = entries.firstOrNull { it.storageKey == key }
    }
}

data class Dosage(val value: String, val unit: DosageUnit)

enum class ExpiryStatus { EXPIRED, EXPIRING_THIS_MONTH, VALID }

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: Dosage? = null,
    val patientCategory: PatientCategory = PatientCategory.HUMAN,
    val patientName: String? = null,
    val isInTreatment: Boolean = false,
    val reminderTimes: List<LocalTime> = emptyList(),
    val photoPath: String? = null,
    val expiry: YearMonth,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun expiryStatus(today: YearMonth): ExpiryStatus = when {
        expiry.isBefore(today) -> ExpiryStatus.EXPIRED
        expiry == today -> ExpiryStatus.EXPIRING_THIS_MONTH
        else -> ExpiryStatus.VALID
    }

    /** Reminders only apply while the medication is in treatment. */
    val activeReminderTimes: List<LocalTime>
        get() = if (isInTreatment) reminderTimes else emptyList()
}
