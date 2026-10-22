package com.windscribe.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主操作按钮：薄荷渐变 + 深色文字，禁用态为半透明玻璃。
 * 登录、注册、两步验证、紧急连接、各类失败页与弹窗共用。
 */
@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .softGlow(Color(0xFF5BF3C6), enabled, 15.dp)
                .clip(shape)
                .background(
                    if (enabled) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF5BF3C6), Color(0xFF2FDCAC))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0x14FFFFFF), Color(0x0AFFFFFF))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (enabled) Color(0x665BF3C6) else Color(0x1AFFFFFF),
                    shape = shape
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color(0xFF04120C) else Color(0xFF6E7C89),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun NextButtonEnabledPreview() {
    NextButton(
        text = "下一步",
        enabled = true,
    ) { }
}

@Preview
@Composable
fun NextButtonDisabledPreview() {
    NextButton(
        text = "下一步",
        enabled = false,
    ) { }
}
