package com.aviva.controlfarmacia.data.mapper

import com.aviva.controlfarmacia.data.local.entity.MedicationEntity
import com.aviva.controlfarmacia.domain.model.Dosage
import com.aviva.controlfarmacia.domain.model.DosageUnit
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.model.PatientCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime
import java.time.YearMonth

class MedicationMapperTest {

    @Test
    fun `domain to entity and back is lossless`() {
        val medication = Medication(
            id = 7,
            name = "Amoxicillin",
            dosage = Dosage("500", DosageUnit.MILLIGRAM),
            patientCategory = PatientCategory.PET,
            patientName = "Luna",
            isInTreatment = true,
            reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 30)),
            photoPath = "/photos/a.jpg",
            expiry = YearMonth.of(2027, 3),
            createdAt = 123L
        )

        assertEquals(medication, medication.toEntity().toDomain())
    }

    @Test
    fun `entity stores the legacy string formats`() {
        val entity = Medication(
            name = "X",
            dosage = Dosage("2", DosageUnit.TABLET),
            reminderTimes = listOf(LocalTime.of(7, 5)),
            expiry = YearMonth.of(2026, 1)
        ).toEntity()

        assertEquals("tablets", entity.dosageUnit)
        assertEquals("HUMAN", entity.patientCategory)
        assertEquals(listOf("07:05"), entity.reminderTimes)
        assertEquals(1, entity.expiryMonth)
        assertEquals(2026, entity.expiryYear)
    }

    @Test
    fun `invalid stored values degrade gracefully`() {
        val entity = MedicationEntity(
            name = "X",
            dosageValue = "10",
            dosageUnit = "furlongs",
            patientCategory = "ALIEN",
            patientName = "   ",
            reminderTimes = listOf("08:00", "not a time", "08:00", "25:99"),
            photoPath = null,
            expiryMonth = 5,
            expiryYear = 2026
        )

        val medication = entity.toDomain()

        assertNull(medication.dosage)
        assertEquals(PatientCategory.HUMAN, medication.patientCategory)
        assertNull(medication.patientName)
        assertEquals(listOf(LocalTime.of(8, 0)), medication.reminderTimes)
    }

    @Test
    fun `dosage without value is dropped`() {
        val entity = MedicationEntity(
            name = "X", dosageValue = "", dosageUnit = "mg", photoPath = null, expiryMonth = 1, expiryYear = 2027
        )
        assertNull(entity.toDomain().dosage)
    }
}
