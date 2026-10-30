package com.aviva.controlfarmacia.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.aviva.controlfarmacia.R

/** Handles the "mark as taken" action of an intake reminder notification. */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_TAKEN) return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId < 0) return
        NotificationManagerCompat.from(context).cancel(notificationId)
        Toast.makeText(context, R.string.reminder_marked_as_taken, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.aviva.controlfarmacia.action.MARK_TAKEN"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
