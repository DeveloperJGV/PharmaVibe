package com.aviva.controlfarmacia.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aviva.controlfarmacia.data.mapper.toStorageString
import com.aviva.controlfarmacia.data.notification.NotificationIds
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.domain.reminder.ReminderScheduler
import com.aviva.controlfarmacia.domain.reminder.ReminderTiming
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ReminderScheduler] backed by [AlarmManager]. One alarm per reminder time; the alarm receiver
 * re-arms itself for the next day, so only the next occurrence is ever scheduled.
 */
@Singleton
class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(medication: Medication) {
        medication.activeReminderTimes
            .take(NotificationIds.MAX_REMINDERS_PER_MEDICATION)
            .forEachIndexed { index, time -> scheduleNext(medication.id, index, time) }
    }

    override fun cancel(medication: Medication) {
        for (index in 0 until NotificationIds.MAX_REMINDERS_PER_MEDICATION) {
            val existing = pendingIntent(medication.id, index, null, PendingIntent.FLAG_NO_CREATE) ?: continue
            alarmManager.cancel(existing)
            existing.cancel()
        }
    }

    /** Arms the alarm for the next occurrence of [time] (today if still ahead, otherwise tomorrow). */
    fun scheduleNext(medicationId: Long, index: Int, time: LocalTime) {
        val trigger = ReminderTiming.nextTriggerAt(time, LocalDateTime.now())
        val triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = pendingIntent(medicationId, index, time, PendingIntent.FLAG_UPDATE_CURRENT)
            ?: return
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun pendingIntent(medicationId: Long, index: Int, time: LocalTime?, extraFlags: Int): PendingIntent? {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_REMINDER
            putExtra(ReminderAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(ReminderAlarmReceiver.EXTRA_INDEX, index)
            time?.let { putExtra(ReminderAlarmReceiver.EXTRA_TIME, it.toStorageString()) }
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.reminder(medicationId, index),
            intent,
            extraFlags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
}
