import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.helper.hapticClickableRipple
import com.windscribe.mobile.ui.theme.AppColors
import com.windscribe.mobile.ui.theme.font18
import com.windscribe.mobile.ui.theme.font24
import com.windscribe.mobile.ui.theme.primaryTextColor

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun NavBar(
    title: String,
    onNavClick: () -> Unit,
) {
    val interactionSource = MutableInteractionSource()
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 8.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow_left),
            contentDescription = stringResource(id = com.windscribe.vpn.R.string.image_description),
            colorFilter = ColorFilter.tint(AppColors.white),
            modifier =
                Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, color = AppColors.white),
                        onClick = onNavClick,
                    ).padding(4.dp)
                    .align(Alignment.CenterStart),
        )

        Text(
            text = title,
            style = font24,
            color = AppColors.white,
            maxLines = 1,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .align(Alignment.Center),
        )
    }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun PreferencesNavBar(
    title: String,
    onNavClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0x660D1218))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(11.dp))
                    .hapticClickableRipple(onClick = onNavClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = stringResource(id = com.windscribe.vpn.R.string.image_description),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryTextColor),
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = font18,
            color = MaterialTheme.colorScheme.primaryTextColor,
            maxLines = 1,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    NavBar(title = "Preferences") {}
}
