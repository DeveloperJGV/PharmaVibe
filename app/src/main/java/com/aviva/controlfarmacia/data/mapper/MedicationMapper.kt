package com.aviva.controlfarmacia.data.mapper

import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun MedicationEntity.toDomain(): Medication = Medication(
    id = id,
    name = name,
    dosage = dosageValue
        ?.takeIf { it.isNotBlank() }
        ?.let { value -> DosageUnit.fromStorageKey(dosageUnit)?.let { Dosage(value, it) } },
    patientCategory = PatientCategory.entries.firstOrNull { it.name == patientCategory }
        ?: PatientCategory.HUMAN,
    patientName = patientName?.takeIf { it.isNotBlank() },
    isInTreatment = isCurrentlyInTreatment,
    reminderTimes = reminderTimes.mapNotNull { it.toLocalTimeOrNull() }.distinct().sorted(),
    photoPath = photoPath,
    expiry = YearMonth.of(expiryYear, expiryMonth),
    createdAt = createdAt
)

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    name = name,
    dosageValue = dosage?.value,
    dosageUnit = dosage?.unit?.storageKey,
    patientCategory = patientCategory.name,
    patientName = patientName,
    isCurrentlyInTreatment = isInTreatment,
    reminderTimes = reminderTimes.map { it.toStorageString() },
    photoPath = photoPath,
    expiryMonth = expiry.monthValue,
    expiryYear = expiry.year,
    createdAt = createdAt
)

fun LocalTime.toStorageString(): String = format(TIME_FORMAT)

fun String.toLocalTimeOrNull(): LocalTime? = try {
    LocalTime.parse(this, TIME_FORMAT)
} catch (e: DateTimeParseException) {
    null
}
