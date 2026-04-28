package com.amro.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amro.core.ui.theme.spacing

@Composable
fun ErrorState(
    message: UiText,
    primaryActionLabel: UiText? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: UiText? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message.asString(), style = MaterialTheme.typography.bodyLarge)

        val hasAnyActions =
            (primaryActionLabel != null && onPrimaryAction != null) ||
                (secondaryActionLabel != null && onSecondaryAction != null)

        if (hasAnyActions) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        }

        if (primaryActionLabel != null && onPrimaryAction != null) {
            Button(onClick = onPrimaryAction) {
                Text(primaryActionLabel.asString())
            }
        }

        if (secondaryActionLabel != null && onSecondaryAction != null) {
            if (primaryActionLabel != null && onPrimaryAction != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
            }
            OutlinedButton(onClick = onSecondaryAction) {
                Text(secondaryActionLabel.asString())
            }
        }
    }
}

