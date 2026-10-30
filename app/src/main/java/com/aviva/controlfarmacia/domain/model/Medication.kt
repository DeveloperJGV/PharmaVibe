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
    /** Units left in the box (tablets, doses...). `null` when the user does not track it. */
    val quantity: Int? = null,
    /** [quantity] at or below this value counts as low stock. */
    val lowStockThreshold: Int = DEFAULT_LOW_STOCK_THRESHOLD,
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

    val isLowStock: Boolean
        get() = quantity != null && quantity <= lowStockThreshold

    /** One unit consumed; never goes below zero and stays `null` when not tracked. */
    fun afterIntake(): Medication = copy(quantity = quantity?.let { (it - 1).coerceAtLeast(0) })

    companion object {
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5
    }
}
