package com.windscribe.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.windscribe.mobile.ui.theme.font18

/**
 * Primary button component with gradient background based on Figma design.
 *
 * States:
 * - Default/Enabled: White to gray-blue gradient background, dark green text
 * - Disabled: Semi-transparent white background, light gray text
 *
 * @param modifier Modifier to be applied to the button
 * @param text Button text
 * @param enabled Whether the button is enabled
 * @param onClick Callback when button is clicked
 */
@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            text = text,
            style =
                font18.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                ),
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF090E19)
fun PrimaryButtonPreview() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
    ) {
        // Enabled state
        Text(
            text = "Enabled State",
            style = font18,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        PrimaryButton(
            text = "Continue",
            enabled = true,
            onClick = { },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Disabled state
        Text(
            text = "Disabled State",
            style = font18,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        PrimaryButton(
            text = "Continue",
            enabled = false,
            onClick = { },
        )
    }
}
