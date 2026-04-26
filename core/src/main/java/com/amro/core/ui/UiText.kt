package com.amro.core.ui

import androidx.annotation.StringRes as StringResId
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    data class StringRes(
        @param:StringResId val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.Dynamic -> value
        is UiText.StringRes -> stringResource(resId, *args.toTypedArray())
    }

