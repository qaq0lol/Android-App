import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.helper.hapticClickableRipple
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
                .padding(top = 12.dp, bottom = 12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, color = MaterialTheme.colorScheme.primary),
                        onClick = onNavClick,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = stringResource(id = com.windscribe.vpn.R.string.image_description),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp).padding(4.dp),
            )
        }

        Text(
            text = title,
            style = font24,
            color = MaterialTheme.colorScheme.onBackground,
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
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .hapticClickableRipple(onClick = onNavClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = stringResource(id = com.windscribe.vpn.R.string.image_description),
                tint = MaterialTheme.colorScheme.primaryTextColor,
                modifier = Modifier.size(24.dp).padding(4.dp),
            )
        }

        Text(
            text = title,
            style = font18,
            color = MaterialTheme.colorScheme.primaryTextColor,
            maxLines = 1,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .align(Alignment.Center),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    NavBar(title = "Preferences") {}
}
