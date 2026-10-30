package com.aviva.controlfarmacia.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.data.mapper.toLocalTimeOrNull
import com.aviva.controlfarmacia.domain.repository.IntakeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

/** Handles the "mark as taken" action of an intake reminder: logs the dose and dismisses it. */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var intakeRepository: IntakeRepository
    @Inject lateinit var clock: Clock

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_TAKEN) return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val scheduledTime = intent.getStringExtra(EXTRA_TIME)?.toLocalTimeOrNull()
        if (notificationId < 0 || medicationId < 0) return

        NotificationManagerCompat.from(context).cancel(notificationId)
        Toast.makeText(context, R.string.reminder_marked_as_taken, Toast.LENGTH_SHORT).show()

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                withTimeoutOrNull(WORK_TIMEOUT_MS) {
                    intakeRepository.recordIntake(medicationId, LocalDateTime.now(clock), scheduledTime)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.aviva.controlfarmacia.action.MARK_TAKEN"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_TIME = "time"

        private const val WORK_TIMEOUT_MS = 8_000L
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
