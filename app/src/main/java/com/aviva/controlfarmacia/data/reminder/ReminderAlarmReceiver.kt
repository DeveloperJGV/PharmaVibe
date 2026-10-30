package com.aviva.controlfarmacia.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aviva.controlfarmacia.data.mapper.toLocalTimeOrNull
import com.aviva.controlfarmacia.data.notification.MedicationNotifier
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Fires at a reminder time. Reads the medication from the database (never trusting stale extras),
 * posts the notification if the reminder is still active and re-arms the alarm for tomorrow.
 */
@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var notifier: MedicationNotifier
    @Inject lateinit var scheduler: AlarmReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val index = intent.getIntExtra(EXTRA_INDEX, -1)
        val time = intent.getStringExtra(EXTRA_TIME)?.toLocalTimeOrNull()
        if (medicationId < 0 || index < 0 || time == null) return

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                withTimeoutOrNull(WORK_TIMEOUT_MS) {
                    val medication = repository.getById(medicationId)
                    if (medication != null && time in medication.activeReminderTimes) {
                        notifier.showIntakeReminder(medication, time, index)
                        scheduler.scheduleNext(medicationId, index, time)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.aviva.controlfarmacia.action.REMINDER"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_INDEX = "index"
        const val EXTRA_TIME = "time"

        /** Broadcast receivers get ~10 s after goAsync(); stay well below it. */
        private const val WORK_TIMEOUT_MS = 8_000L
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
