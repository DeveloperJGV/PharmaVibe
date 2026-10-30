package com.aviva.controlfarmacia.data.reminder

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aviva.controlfarmacia.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Alarms do not survive a reboot, a clock/time-zone change or an exact-alarm permission change.
 * Re-arms every active treatment reminder when any of those happens.
 */
@AndroidEntryPoint
class RescheduleRemindersReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var scheduler: AlarmReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                withTimeoutOrNull(WORK_TIMEOUT_MS) {
                    repository.getAllInTreatment().forEach(scheduler::schedule)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_EXACT_ALARM_PERMISSION_CHANGED
        )

        /** [AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED]; only sent on API 31+. */
        const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        const val WORK_TIMEOUT_MS = 8_000L
        val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
