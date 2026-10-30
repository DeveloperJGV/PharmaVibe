package com.aviva.controlfarmacia.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Every migration between released schema versions. Keep in sync with `app/schemas/`. */
object Migrations {

    /** v4: stock tracking on medications and the intake log. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medications ADD COLUMN quantity INTEGER")
            db.execSQL("ALTER TABLE medications ADD COLUMN lowStockThreshold INTEGER NOT NULL DEFAULT 5")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS intakes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    medicationId INTEGER NOT NULL,
                    takenAt INTEGER NOT NULL,
                    scheduledTime TEXT,
                    FOREIGN KEY(medicationId) REFERENCES medications(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_intakes_medicationId ON intakes (medicationId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_intakes_takenAt ON intakes (takenAt)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_3_4)
}
