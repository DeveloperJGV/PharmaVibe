package com.aviva.controlfarmacia.ui.common

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Returns an action that asks for POST_NOTIFICATIONS when it is not granted yet.
 * Below Android 13 the permission does not exist, so the action is a no-op.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionRequest(): () -> Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return {}
    val state = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    return { if (!state.status.isGranted) state.launchPermissionRequest() }
}
