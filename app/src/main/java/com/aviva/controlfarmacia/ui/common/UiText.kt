package com.aviva.controlfarmacia.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Text that can be resolved lazily, so view models never touch Android resources. */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId, *args)
    }

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> context.getString(resId, *args)
    }
}
