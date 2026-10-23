package com.windscribe.mobile.ui.preferences.lipstick

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.nav.LocalNavController

@Composable
fun WallpaperEditorScreen() {
    val activity = LocalActivity.current as? AppStartActivity
    val viewmodel: LipstickViewmodel = activity?.let {
        hiltViewModel<LipstickViewmodelImpl>(viewModelStoreOwner = it)
    } ?: hiltViewModel<LipstickViewmodelImpl>()

    val navController = LocalNavController.current
    val context = LocalContext.current
    val config by viewmodel.globalBackgroundConfig.collectAsState()

    var scale by remember(config.scale) { mutableFloatStateOf(config.scale) }
    var offsetX by remember(config.offsetX) { mutableFloatStateOf(config.offsetX) }
    var offsetY by remember(config.offsetY) { mutableFloatStateOf(config.offsetY) }
    var rotation by remember(config.rotation) { mutableIntStateOf(config.rotation) }
    var darkness by remember(config.darkness) { mutableFloatStateOf(config.darkness) }

    var isFillMode by remember { mutableStateOf(true) }
    var showMockUI by remember { mutableStateOf(true) }
    var showOpacityPanel by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewmodel.setGlobalBackgroundFromUri(context, uri)
            scale = 1.0f
            offsetX = 0f
            offsetY = 0f
            rotation = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ================= LAYER 1: GPU-ACCELERATED FULL WALLPAPER CANVAS =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 5.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                        viewmodel.updateGlobalBackgroundTransform(scale, offsetX, offsetY, rotation)
                    }
                }
        ) {
            if (config.bitmap != null) {
                WallpaperCanvas(
                    bitmap = config.bitmap,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    rotation = rotation,
                    darkness = darkness,
                    fillMode = isFillMode,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF06080D)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "未载入图片，请从相册选择",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1B6EF3))
                                .clickable { galleryLauncher.launch("image/*") }
                                .padding(horizontal = 18.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🖼️ 选择相册图片",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ================= TOP FLOATING ISLAND =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xE6141B24))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x331B6EF3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔍", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "双指缩放 · 自由漫游平移",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "完整像素保真 · 绝无暴力裁切",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Change image button directly in editor
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable { galleryLauncher.launch("image/*") }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🖼️ 换图",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Eye UI Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (showMockUI) Color(0xFF1B6EF3) else Color(0xFF475569))
                            .clickable { showMockUI = !showMockUI }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showMockUI) "👁 UI: 开" else "👁 UI: 关",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ================= MIDDLE OVERLAY MOCK UI CARDS =================
        AnimatedVisibility(
            visible = showMockUI,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Windscribe",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC0D1218))
                            .border(1.dp, Color(0x331B6EF3), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "UI效果预览中",
                            color = Color(0xFF60A5FA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sample Card 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xB3141D28))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "东京 · 极速游戏专线",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "延迟 21ms · 负载 14%",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "›",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sample Card 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xB3141D28))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "今日全局流量统计",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "共使用 1.8 GB · 剩余 8.2 GB",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "›",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ================= BOTTOM FLOATING DOCK =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expandable Darkness & Scale Slider Panel
            AnimatedVisibility(visible = showOpacityPanel) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xEB111827))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🌑 暗度遮罩 (Darkness Mask)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(darkness * 100).toInt()}%",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = darkness,
                            onValueChange = {
                                darkness = it
                                viewmodel.updateGlobalBackgroundDarkness(it)
                            },
                            valueRange = 0f..0.85f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔍 缩放倍率 (Scale)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = scale,
                            onValueChange = {
                                scale = it
                                viewmodel.updateGlobalBackgroundTransform(scale, offsetX, offsetY, rotation)
                            },
                            valueRange = 0.4f..3.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }

            // Main Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xE6141D28))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: 4 Action Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. 旋转 90°
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable {
                                    rotation = (rotation + 90) % 360
                                    viewmodel.updateGlobalBackgroundTransform(scale, offsetX, offsetY, rotation)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔄", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "旋转",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 2. 铺满 / 完整 显示模式切换
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isFillMode) Color(0x331B6EF3) else Color(0x1AFFFFFF))
                                .clickable {
                                    isFillMode = !isFillMode
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                    viewmodel.updateGlobalBackgroundTransform(scale, offsetX, offsetY, rotation)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (isFillMode) "📐" else "🔲", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isFillMode) "铺满" else "完整",
                                    color = if (isFillMode) Color(0xFF60A5FA) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3. 复位居中
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable {
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0
                                    viewmodel.resetGlobalBackgroundPosition()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "↺", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "居中",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 4. 透明度
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (showOpacityPanel) Color(0x333B82F6) else Color(0x1AFFFFFF))
                                .clickable { showOpacityPanel = !showOpacityPanel },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💧", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "暗度",
                                    color = if (showOpacityPanel) Color(0xFF60A5FA) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Row 2: Cancel and Save Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable {
                                    viewmodel.cancelGlobalBackgroundEdit()
                                    navController.popBackStack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "取消",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1B6EF3))
                                .clickable {
                                    viewmodel.saveAndApplyGlobalBackground()
                                    navController.popBackStack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "保存并应用",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
