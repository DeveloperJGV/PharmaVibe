package com.aviva.controlfarmacia.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aviva.controlfarmacia.MainActivity
import com.aviva.controlfarmacia.R
import com.aviva.controlfarmacia.data.mapper.toStorageString
import com.aviva.controlfarmacia.data.reminder.ReminderActionReceiver
import com.aviva.controlfarmacia.domain.model.Medication
import com.aviva.controlfarmacia.ui.common.labelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import javax.inject.Singleton

/** Builds and posts every notification of the app. Channels are created once in [ensureChannels]. */
@Singleton
class MedicationNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)

    fun ensureChannels() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_EXPIRATION, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel_expiration_name))
                .setDescription(context.getString(R.string.notification_channel_expiration_description))
                .build()
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_REMINDERS, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel_reminders_name))
                .setDescription(context.getString(R.string.notification_channel_reminders_description))
                .build()
        )
    }

    fun showExpirationSummary(expired: List<Medication>, expiringSoon: List<Medication>) {
        if (expired.isNotEmpty()) {
            notify(
                id = NotificationIds.EXPIRED,
                channel = CHANNEL_EXPIRATION,
                title = context.getString(R.string.notification_expired_title),
                text = quantityText(R.plurals.notification_expired_text, expired),
                category = NotificationCompat.CATEGORY_REMINDER
            )
        }
        if (expiringSoon.isNotEmpty()) {
            notify(
                id = NotificationIds.EXPIRING_SOON,
                channel = CHANNEL_EXPIRATION,
                title = context.getString(R.string.notification_expiring_title),
                text = quantityText(R.plurals.notification_expiring_text, expiringSoon),
                category = NotificationCompat.CATEGORY_REMINDER
            )
        }
    }

    fun showLowStock(lowStock: List<Medication>) {
        if (lowStock.isEmpty()) return
        notify(
            id = NotificationIds.LOW_STOCK,
            channel = CHANNEL_EXPIRATION,
            title = context.getString(R.string.notification_low_stock_title),
            text = quantityText(R.plurals.notification_low_stock_text, lowStock),
            category = NotificationCompat.CATEGORY_REMINDER
        )
    }

    fun showIntakeReminder(medication: Medication, time: LocalTime, index: Int) {
        val id = NotificationIds.reminder(medication.id, index)
        val dosage = medication.dosage?.let { "${it.value} ${context.getString(it.unit.labelRes())}" }
        val formattedTime = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val text = when {
            dosage != null && medication.patientName != null ->
                context.getString(R.string.notification_reminder_text_dosage_patient, dosage, medication.patientName, formattedTime)
            dosage != null ->
                context.getString(R.string.notification_reminder_text_dosage, dosage, formattedTime)
            medication.patientName != null ->
                context.getString(R.string.notification_reminder_text_patient, medication.patientName, formattedTime)
            else -> context.getString(R.string.notification_reminder_text, formattedTime)
        }

        val markTakenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_MARK_TAKEN
            putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, id)
            putExtra(ReminderActionReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(ReminderActionReceiver.EXTRA_TIME, time.toStorageString())
        }
        val markTaken = NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_action_mark_taken),
            PendingIntent.getBroadcast(context, id, markTakenIntent, PENDING_INTENT_FLAGS)
        ).build()

        notify(
            id = id,
            channel = CHANNEL_REMINDERS,
            title = medication.name,
            text = text,
            category = NotificationCompat.CATEGORY_ALARM,
            action = markTaken
        )
    }

    fun cancel(id: Int) = manager.cancel(id)

    private fun quantityText(pluralsRes: Int, medications: List<Medication>): String =
        context.resources.getQuantityString(
            pluralsRes, medications.size, medications.size, medications.joinToString { it.name }
        )

    private fun notify(
        id: Int,
        channel: String,
        title: String,
        text: String,
        category: String,
        action: NotificationCompat.Action? = null
    ) {
        if (!canPostNotifications()) return
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(category)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
        action?.let(builder::addAction)
        try {
            manager.notify(id, builder.build())
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the call; nothing to show.
        }
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PENDING_INTENT_FLAGS)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return manager.areNotificationsEnabled()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val CHANNEL_EXPIRATION = "expiration_alerts"
        const val CHANNEL_REMINDERS = "treatment_reminders"
        const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
