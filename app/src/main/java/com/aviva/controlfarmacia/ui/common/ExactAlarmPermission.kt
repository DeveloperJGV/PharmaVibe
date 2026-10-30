package com.aviva.controlfarmacia.ui.common

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Whether reminders can fire at the exact minute, re-checked every time the screen resumes. */
class ExactAlarmPermissionState(
    val isGranted: Boolean,
    /** Opens the system screen where the user can allow exact alarms. No-op when already granted. */
    val openSettings: () -> Unit
)

@Composable
fun rememberExactAlarmPermissionState(): ExactAlarmPermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.canScheduleExactAlarms()) }

    LifecycleResumeEffect(Unit) {
        granted = context.canScheduleExactAlarms()
        onPauseOrDispose { }
    }

    return remember(granted) {
        ExactAlarmPermissionState(
            isGranted = granted,
            openSettings = {
                if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        )
    }
}

private fun Context.canScheduleExactAlarms(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
