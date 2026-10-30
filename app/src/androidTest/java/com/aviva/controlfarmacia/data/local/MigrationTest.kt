package com.aviva.controlfarmacia.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Runs the real migrations against the exported schemas in `app/schemas/`. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Test
    @Throws(IOException::class)
    fun migrate3To4_keepsRowsAddsStockColumnsAndIntakes() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO medications
                    (name, dosageForm, dosageValue, dosageUnit, patientCategory, patientName,
                     isCurrentlyInTreatment, reminderTimes, photoPath, expiryMonth, expiryYear, createdAt)
                VALUES ('Paracetamol', 'Tablet', '500', 'mg', 'PET', 'Luna', 1, '["08:00"]', NULL, 3, 2028, 42)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, Migrations.MIGRATION_3_4)

        db.query("SELECT id, name, quantity, lowStockThreshold, reminderTimes FROM medications").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Paracetamol", cursor.getString(1))
            assertTrue("quantity starts untracked", cursor.isNull(2))
            assertEquals(5, cursor.getInt(3))
            assertEquals("""["08:00"]""", cursor.getString(4))
        }

        db.execSQL("INSERT INTO intakes (medicationId, takenAt, scheduledTime) VALUES (1, 1000, '08:00')")
        db.query("SELECT COUNT(*) FROM intakes").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        // Cascade: deleting the medication removes its intakes.
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("DELETE FROM medications WHERE id = 1")
        db.query("SELECT COUNT(*) FROM intakes").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
