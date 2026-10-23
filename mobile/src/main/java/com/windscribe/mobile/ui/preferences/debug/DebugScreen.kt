package com.windscribe.mobile.ui.preferences.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.vpn.Windscribe.Companion.appContext
import com.windscribe.vpn.apppreference.PreferencesKeyConstants
import org.json.JSONObject
import java.io.File

private val logJsonRegex = Regex("""^\{"tm":"([^"]*)","lvl":"([^"]*)","mod":"([^"]*)","msg":"(.*)"\}$""")

private data class FormattedLog(
    val displayText: String,
    val color: Color,
)

private fun parseLogEntry(line: String): FormattedLog {
    val trimmed = line.trim()
    var tm: String? = null
    var lvl: String? = null
    var mod: String? = null
    var msg: String? = null

    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        try {
            val json = JSONObject(trimmed)
            tm = json.optString("tm")
            lvl = json.optString("lvl").uppercase()
            mod = json.optString("mod")
            msg = json.optString("msg")
        } catch (_: Exception) {
            val match = logJsonRegex.find(trimmed)
            if (match != null) {
                tm = match.groupValues[1]
                lvl = match.groupValues[2].uppercase()
                mod = match.groupValues[3]
                msg = match.groupValues[4]
            }
        }
    }

    if (!lvl.isNullOrEmpty() && msg != null) {
        val timeDisplay = if (tm?.contains(" ") == true) tm.substringAfter(" ") else (tm ?: "")
        val formattedText = buildString {
            if (timeDisplay.isNotEmpty()) append("[$timeDisplay] ")
            append("[$lvl] ")
            if (!mod.isNullOrEmpty()) append("[$mod] ")
            append(msg)
        }

        val color = when (lvl) {
            "ERROR" -> Color(0xFFFF5252)
            "WARN" -> Color(0xFFFFD54F)
            "INFO" -> NeonGreen
            "DEBUG" -> Color(0xEE81E6D9)
            else -> when {
                formattedText.contains("error", ignoreCase = true) || formattedText.contains("fail", ignoreCase = true) -> Color(0xFFFF5252)
                formattedText.contains("warn", ignoreCase = true) -> Color(0xFFFFD54F)
                formattedText.contains("connected", ignoreCase = true) || formattedText.contains("success", ignoreCase = true) -> NeonGreen
                else -> Color(0xEE81E6D9)
            }
        }

        return FormattedLog(displayText = formattedText, color = color)
    }

    val color = when {
        line.contains("error", ignoreCase = true) || line.contains("fail", ignoreCase = true) -> Color(0xFFFF5252)
        line.contains("warn", ignoreCase = true) -> Color(0xFFFFD54F)
        line.contains("connected", ignoreCase = true) || line.contains("success", ignoreCase = true) -> NeonGreen
        else -> Color(0xEE81E6D9)
    }

    return FormattedLog(displayText = line, color = color)
}

@Composable
fun DebugScreen(viewModel: DebugViewModel = hiltViewModel<DebugViewModelImpl>()) {
    val debugLog by viewModel.debugLog.collectAsState()
    val showProgress by viewModel.showProgress.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    DebugContent(
        debugLog = debugLog,
        showProgress = showProgress,
        onRefresh = { viewModel.refresh() },
    )
}

@Composable
fun DebugContent(
    debugLog: List<String>,
    showProgress: Boolean,
    onRefresh: () -> Unit,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(debugLog.size) {
        if (debugLog.isNotEmpty()) {
            listState.scrollToItem(debugLog.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "运行日志",
                subtitle = "REAL-TIME ENGINE DIAGNOSTIC LOG",
                onBack = { navController.popBackStack() },
                trailingContent = {
                    CyberBadge(
                        text = "${debugLog.size} 条记录",
                        color = NeonGreen
                    )
                }
            )

            // Terminal View Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF0080D17))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                if (debugLog.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (showProgress) {
                            CircularProgressIndicator(color = NeonCyan)
                        } else {
                            Text(
                                text = "日志记录引擎运行中，暂无最新网络交互日志。可点击下方【刷新】获取最新日志",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(debugLog) { index, line ->
                            val logEntry = remember(line) { parseLogEntry(line) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            ) {
                                Text(
                                    text = String.format("%03d ", index + 1),
                                    color = Color(0x40FFFFFF),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = logEntry.displayText,
                                    color = logEntry.color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions Bar
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberButton(
                        text = "刷新",
                        onClick = onRefresh,
                        modifier = Modifier.weight(1f),
                        color = NeonCyan
                    )

                    CyberButton(
                        text = "复制完整日志",
                        onClick = {
                            val fullLog = debugLog.joinToString("\n")
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DebugLog", fullLog))
                            Toast.makeText(context, "已复制 ${debugLog.size} 行日志到剪贴板", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.3f),
                        color = Color(0xFFFFB74D)
                    )

                    CyberButton(
                        text = "导出文件",
                        onClick = { exportLog(context) },
                        modifier = Modifier.weight(1f),
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun exportLog(context: Context) {
    val logFile = File(appContext.filesDir.path + PreferencesKeyConstants.DEBUG_LOG_FILE_NAME)
    if (logFile.exists() && logFile.length() > 0) {
        try {
            val fileUri: Uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    logFile,
                )
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            val chooser = Intent.createChooser(shareIntent, "Export Log File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "未找到可用于分享日志的应用", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "日志文件尚未生成", Toast.LENGTH_SHORT).show()
    }
}
