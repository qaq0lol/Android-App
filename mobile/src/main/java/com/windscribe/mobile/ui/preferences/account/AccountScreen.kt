package com.windscribe.mobile.ui.preferences.account

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberBandwidthGauge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberTextField
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.GlassSurfaceColor
import com.windscribe.mobile.ui.common.LocalGlassColors
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.NeonYellow
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.common.openUrl
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.mobile.ui.popup.FullScreenDialogState
import com.windscribe.mobile.ui.popup.HandleFullScreenDialog
import kotlinx.coroutines.flow.Flow

/**
 * Callbacks the account UI can raise.
 */
class AccountActions(
    val onManageAccountClicked: () -> Unit = {},
    val onResetPasswordClicked: () -> Unit = {},
    val onVoucherCodeClicked: () -> Unit = {},
    val onLazyLoginClicked: () -> Unit = {},
    val onEnterVoucherCode: (String) -> Unit = {},
    val onEnterLazyLoginCode: (String) -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
)

@Composable
fun AccountScreen(viewModel: AccountViewModel = hiltViewModel<AccountViewModelImpl>()) {
    val showProgress by viewModel.showProgress.collectAsState()
    val isGhostAccount by viewModel.isGhostAccount.collectAsState()
    val accountState by viewModel.accountState.collectAsState()
    val isSsoLogin by viewModel.isSsoLogin.collectAsState()

    AccountContent(
        showProgress = showProgress,
        isGhostAccount = isGhostAccount,
        accountState = accountState,
        isSsoLogin = isSsoLogin,
        alertState = viewModel.alertState,
        goTo = viewModel.goTo,
        actions = AccountActions(
            onManageAccountClicked = viewModel::onManageAccountClicked,
            onResetPasswordClicked = viewModel::onResetPasswordClicked,
            onVoucherCodeClicked = viewModel::onVoucherCodeClicked,
            onLazyLoginClicked = viewModel::onLazyLoginClicked,
            onEnterVoucherCode = viewModel::onEnterVoucherCode,
            onEnterLazyLoginCode = viewModel::onEnterLazyLoginCode,
            onDialogDismiss = viewModel::onDialogDismiss,
        ),
    )
}

@Composable
fun AccountContent(
    showProgress: Boolean,
    isGhostAccount: Boolean,
    accountState: AccountState,
    isSsoLogin: Boolean,
    alertState: Flow<AlertState>,
    goTo: Flow<AccountGoTo>,
    actions: AccountActions,
) {
    val navController = LocalNavController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CyberTopAppBar(
                title = "账户",
                subtitle = "ACCOUNT CREDENTIALS & SUBSCRIPTION",
                onBack = { navController.popBackStack() },
                trailingContent = {
                    if (showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            if (isGhostAccount) {
                CyberGhostAccountState()
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 订购计划（主视觉前置）
                CyberPlanInfoCard(accountState = accountState, onManageClick = actions.onManageAccountClicked)

                Spacer(modifier = Modifier.height(12.dp))

                // 账户信息
                CyberAccountInfoCard(accountState = accountState)

                if (isSsoLogin && accountState.emailState is EmailState.Email) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CyberButton(
                        text = "重置密码",
                        onClick = actions.onResetPasswordClicked,
                        modifier = Modifier.fillMaxWidth(),
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 其他操作
                CyberOtherActionsCard(
                    onVoucherClick = actions.onVoucherCodeClicked,
                    onLazyLoginClick = actions.onLazyLoginClicked
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        HandleGoto(goTo)
        HandleAlertState(alertState, actions)
    }
}

@Composable
private fun CyberAccountInfoCard(accountState: AccountState) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val account = accountState as? AccountState.Account

    val username = account?.username ?: if (accountState is AccountState.Loading) "加载中..." else "Unknown"
    val emailState = account?.emailState ?: EmailState.NoEmail
    val isHashedAccount = username.startsWith("0x")

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Username Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Username", username))
                        Toast.makeText(context, "用户名已复制", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1FFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_myaccount_icon),
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isHashedAccount) "Hash 标识" else "用户名",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CyberBadge(text = "复制", color = NeonCyan)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LocalGlassColors.current.borderSubtle))

            // Email Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x264FF0C0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_preferences_icon),
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "绑定邮箱",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (emailState) {
                    is EmailState.Email -> {
                        Text(
                            text = emailState.email,
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is EmailState.UnconfirmedEmail -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { navController.navigate(Screen.ConfirmEmail.route) }
                        ) {
                            Text(
                                text = emailState.email,
                                color = NeonYellow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            CyberBadge(text = "待验证", color = NeonYellow)
                        }
                    }
                    is EmailState.NoEmail -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { navController.navigate(Screen.AddEmail.route) }
                        ) {
                            Text(
                                text = "未绑定",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            CyberBadge(text = "+ 领取 10GB", color = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberPlanInfoCard(
    accountState: AccountState,
    onManageClick: () -> Unit
) {
    val account = accountState as? AccountState.Account
    val type = account?.type
    val dateType = account?.dateType

    val isPro = type is AccountType.Pro || type is AccountType.Unlimited
    val planBadgeText = when (type) {
        is AccountType.Pro -> "PRO 专业版"
        is AccountType.Unlimited -> "无限流量专线"
        is AccountType.AlcCustom -> "定制专线"
        is AccountType.Free -> "免费账户"
        null -> "检查中..."
    }

    val planDataText = when (type) {
        is AccountType.Pro, is AccountType.Unlimited -> "无限数据流量 (Unlimited)"
        is AccountType.Free -> type.data
        is AccountType.AlcCustom -> type.data
        null -> "--"
    }

    val dateLabel = when (dateType) {
        is DateType.Expiry -> "有效期至"
        is DateType.Reset -> "流量重置日"
        null -> "周期结算"
    }
    val dateValue = dateType?.date ?: "--"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        if (isPro) Color(0x33A78BFA) else Color(0x14FFFFFF),
                        Color(0x0AFFFFFF),
                    )
                )
            )
            .border(
                1.dp,
                if (isPro) Color(0x40A78BFA) else Color(0x1AFFFFFF),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isPro) Color(0x2EA78BFA) else Color(0x1FFFFFFF))
                            .border(
                                1.dp,
                                if (isPro) Color(0x4DA78BFA) else Color(0x22FFFFFF),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = planBadgeText,
                            color = if (isPro) Color(0xFFB299FF) else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(9.dp))
                    Text(
                        text = planDataText,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = account?.username ?: "",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = dateLabel, color = TextMuted, fontSize = 9.5.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = dateValue,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "剩余流量",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = if (isPro) "不限量" else (account?.dataLeft ?: "--"),
                    color = if (isPro) NeonGreen else TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x4D0D1218))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(onClick = onManageClick)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "在网页端管理账户与订阅",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    painter = painterResource(id = R.drawable.link_arrow_icon),
                    contentDescription = null,
                    tint = Color(0xFFB299FF),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CyberOtherActionsCard(
    onVoucherClick: () -> Unit,
    onLazyLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Voucher
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onVoucherClick),
            cornerRadius = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x26B299FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_myaccount_icon),
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "礼券兑换码",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "将礼券或升级代码应用到您的账户",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward_arrow_white),
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Lazy Login
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLazyLoginClick),
            cornerRadius = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1FFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_preferences_icon),
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "快速配对登录",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "登录 TV 应用程序或在此输入快速配对码",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward_arrow_white),
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CyberGhostAccountState() {
    val navController = LocalNavController.current
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text(
            text = "临时匿名账户",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "您可以随时登录已有账户，或将此临时账户转为正式注册账户。",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        CyberButton(
            text = "登录已有账户",
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        CyberButton(
            text = "转为正式注册账户",
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set("isAccountClaim", true)
                navController.navigate(Screen.Signup.route)
            },
            modifier = Modifier.fillMaxWidth(),
            color = NeonGreen
        )
    }
}

@Composable
private fun HandleGoto(goToFlow: Flow<AccountGoTo>) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val goTo by goToFlow.collectAsState(initial = AccountGoTo.None)

    LaunchedEffect(goTo) {
        when (val target = goTo) {
            is AccountGoTo.ManageAccount -> context.openUrl(target.url)
            is AccountGoTo.Upgrade -> navController.navigate(Screen.Upgrade.route)
            is AccountGoTo.Error -> Toast.makeText(context, target.message, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }
}

@Composable
private fun HandleAlertState(
    alertStateFlow: Flow<AlertState>,
    actions: AccountActions,
) {
    val activity = LocalActivity.current as? AppStartActivity
    val alertState by alertStateFlow.collectAsState(initial = AlertState.None)
    var showVoucherDialog by remember { mutableStateOf(false) }
    var showLazyLoginDialog by remember { mutableStateOf(false) }
    var fullScreenDialogState by remember { mutableStateOf<FullScreenDialogState>(FullScreenDialogState.None) }

    LaunchedEffect(alertState) {
        when (alertState) {
            is AlertState.Error -> {
                val result = (alertState as AlertState.Error).message
                val message = when (result) {
                    is ToastMessage.Raw -> result.message
                    is ToastMessage.Localized -> activity?.getString(result.message) ?: ""
                    else -> ""
                }
                fullScreenDialogState = FullScreenDialogState.Error(message)
            }
            is AlertState.Success -> {
                val result = (alertState as AlertState.Success).message
                val message = when (result) {
                    is ToastMessage.Raw -> result.message
                    is ToastMessage.Localized -> activity?.getString(result.message) ?: ""
                    else -> ""
                }
                fullScreenDialogState = FullScreenDialogState.Success(message)
            }
            is AlertState.LazyLogin -> showLazyLoginDialog = true
            is AlertState.VoucherCode -> showVoucherDialog = true
            else -> {}
        }
    }

    HandleFullScreenDialog(
        state = fullScreenDialogState,
        onDismiss = {
            fullScreenDialogState = FullScreenDialogState.None
            actions.onDialogDismiss()
        },
    )

    if (showVoucherDialog) {
        CyberInputDialog(
            title = "礼券兑换码",
            placeholder = "请输入礼券兑换码或充值代码",
            onDismiss = {
                showVoucherDialog = false
                actions.onDialogDismiss()
            },
            onSubmit = { actions.onEnterVoucherCode(it) }
        )
    }

    if (showLazyLoginDialog) {
        CyberInputDialog(
            title = "延迟登录配对",
            placeholder = "请输入 8 位配对代码",
            isLazyLogin = true,
            onDismiss = {
                showLazyLoginDialog = false
                actions.onDialogDismiss()
            },
            onSubmit = { actions.onEnterLazyLoginCode(it) }
        )
    }
}

@Composable
private fun CyberInputDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    isLazyLogin: Boolean = false,
) {
    var textValue by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            cornerRadius = 24.dp,
            borderColor = Color(0x26FFFFFF),
            backgroundColor = Color(0xF20F1524)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                CyberTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (isLazyLogin) {
                            val clean = input.uppercase().replace("-", "").take(8)
                            textValue = if (clean.length > 4) {
                                "${clean.substring(0, 4)}-${clean.substring(4)}"
                            } else {
                                clean
                            }
                            if (clean.length == 8) {
                                onDismiss()
                                onSubmit(textValue)
                            }
                        } else {
                            textValue = input
                        }
                    },
                    placeholder = placeholder,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Characters
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    CyberButton(
                        text = "取消",
                        onClick = onDismiss,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CyberButton(
                        text = "确认提交",
                        onClick = {
                            onDismiss()
                            onSubmit(textValue)
                        },
                        color = NeonCyan
                    )
                }
            }
        }
    }
}
