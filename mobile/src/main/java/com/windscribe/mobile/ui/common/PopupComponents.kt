package com.windscribe.mobile.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.theme.AppColors
import com.windscribe.mobile.ui.theme.font16
import com.windscribe.mobile.ui.theme.font18
import com.windscribe.mobile.ui.theme.font22
import com.windscribe.mobile.ui.theme.primaryTextColor
import com.windscribe.mobile.ui.theme.serverListBackgroundColor

@Composable
fun PopupContainer(content: @Composable (ColumnScope.() -> Unit)) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color(0xF204060A))
            .clickable { },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(274.dp)
                    .verticalScroll(scrollState),
            content = content,
        )
    }
}

@Composable
fun PopupPrimaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .softGlow(Color(0xFF4FF0C0), true, 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x2E4FF0C0), Color(0x0F4FF0C0))
                )
            )
            .border(1.dp, Color(0x884FF0C0), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF4FF0C0),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PopupSecondaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFC6D0DA),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PopupHeroImage(resourceId: Int) {
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "Garry Image",
        modifier =
            Modifier
                .size(width = 274.dp, height = 217.dp),
    )
}

@Composable
fun PopupTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFFF2F6F9),
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun PopupDescription(text: String) {
    Text(
        text = text,
        color = Color(0xFF8A98A6),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}

@Preview
@Composable
fun PopupHeroImagePreview() {
    PopupHeroImage(resourceId = R.drawable.garry_location_under_maintence)
}

@Preview(
    name = "Light Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PopupTitlePreview() {
    PopupTitle(stringResource(com.windscribe.vpn.R.string.under_maintenance))
}

@Preview(
    name = "Light Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PopupDescriptionPreview() {
    PopupDescription(stringResource(com.windscribe.vpn.R.string.check_status_description))
}

@Preview(
    name = "Light Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PopupPrimaryActionButtonPreview() {
    PopupPrimaryActionButton(
        text = stringResource(com.windscribe.vpn.R.string.check_status),
        onClick = {},
    )
}

@Preview(
    name = "Light Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PopupSecondaryActionButtonPreview() {
    PopupSecondaryActionButton(
        text = stringResource(com.windscribe.vpn.R.string.back),
        onClick = {},
    )
}
