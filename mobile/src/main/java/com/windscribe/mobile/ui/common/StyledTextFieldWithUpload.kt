package com.windscribe.mobile.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.theme.AppColors
import com.windscribe.mobile.ui.theme.font16

/**
 * A styled text field component with an upload button for hashed login.
 *
 * States:
 * - Default: Gray border (white 10% opacity), gray placeholder
 * - Active: White border, gray placeholder (focused state)
 * - Error: Red border (#ff7f7f), red text
 *
 * @param modifier Modifier to be applied to the text field
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param placeholder Placeholder text to display when empty
 * @param isError Whether the field is in error state
 * @param onUploadClick Callback for upload button click
 * @param imeAction IME action for the keyboard
 */
@Composable
fun StyledTextFieldWithUpload(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    placeholder: String = "",
    isError: Boolean = false,
    onUploadClick: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Done,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Define colors based on state
    val borderColor =
        when {
            isError -> Color(0xFFFF7F7F)

            // Red border for error state
            isFocused -> MaterialTheme.colorScheme.onSurface

            // White border for active/focused state
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) // Gray border for default state
        }

    val textColor = if (isError) Color(0xFFFF7F7F) else MaterialTheme.colorScheme.onSurface
    val placeholderColor = if (isError) Color(0xFFFF7F7F) else Color(0xFF898F9D)

    Box {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = font16,
                    color = placeholderColor,
                    textAlign = TextAlign.Start,
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = imeAction,
                    keyboardType = KeyboardType.Text,
                ),
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    // Upload button
                    IconButton(
                        onClick = onUploadClick,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_upload),
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            },
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    disabledTextColor = textColor,
                    errorTextColor = textColor,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    errorContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    selectionColors =
                        androidx.compose.foundation.text.selection.TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.onSurface,
                            backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        ),
                ),
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = MaterialTheme.shapes.medium,
                    ).border(
                        width = 1.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.medium,
                    ),
            textStyle =
                font16.copy(
                    color = textColor,
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp,
                ),
            interactionSource = interactionSource,
        )
    }
}
