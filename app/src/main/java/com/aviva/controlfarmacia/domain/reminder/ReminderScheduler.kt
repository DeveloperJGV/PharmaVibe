package com.aviva.controlfarmacia.domain.reminder

import com.aviva.controlfarmacia.domain.model.Medication

/** Schedules the daily intake reminders of a medication in treatment. */
interface ReminderScheduler {
    /** Schedules every reminder of [medication]; does nothing if it is not in treatment. */
    fun schedule(medication: Medication)

    /** Cancels every reminder previously scheduled for [medication]. */
    fun cancel(medication: Medication)
}
