package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.RegionId

@Composable
fun WorldMapScreen(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val regions = RegionId.values().toList()
    var selectedRegion by remember { mutableStateOf(engine.currentRegionId) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50B0E14))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "خريطة عالم مملكة الظلال (Dark Realm)",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المنطقة الحالية: ${engine.currentRegionId.displayName}",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF263238), CircleShape)
                        .testTag("close_map")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of 7 Regions
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(regions) { reg ->
                    val isDiscovered = engine.unlockedWaypoints.contains(reg.name)
                    val isCurrent = engine.currentRegionId == reg
                    val isSelected = selectedRegion == reg

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .background(
                                if (isCurrent) Color(0xFF1B3224) else Color(0xFF141A24),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isCurrent) Color(0xFF76FF03) else if (isSelected) Color(0xFFFFD700) else Color(0xFF37474F),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedRegion = reg }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDiscovered) reg.displayName else "؟؟؟ (غير مستكشفة)",
                                    color = if (isCurrent) Color(0xFF76FF03) else if (isDiscovered) Color.White else Color(0xFF78909C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (isCurrent) {
                                    Text(text = "موقعك", color = Color(0xFF76FF03), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "مستوى التهديد: ${reg.dangerLevel}",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "الطقس: ${reg.weatherType.displayName}",
                                color = Color(0xFF90CAF9),
                                fontSize = 10.sp
                            )

                            if (isDiscovered) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "نقطة سفر سريع نشطة ✨",
                                    color = Color(0xFFFFD700),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fast Travel Footer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141A24), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الوجهة المختارة: ${selectedRegion.displayName}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يمكنك الانتقال الفوري إلى أي منطقة تم اكتشاف بوابتها مسبقاً.",
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp
                    )
                }

                val canFastTravel = engine.unlockedWaypoints.contains(selectedRegion.name) && selectedRegion != engine.currentRegionId
                Button(
                    onClick = {
                        if (canFastTravel) {
                            engine.loadRegion(selectedRegion, 7 * 64f, 10 * 64f)
                            engine.audioEngine.playChest()
                            onClose()
                        }
                    },
                    enabled = canFastTravel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        disabledContainerColor = Color(0xFF37474F)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = if (canFastTravel) Color.Black else Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "سفر سريع",
                        color = if (canFastTravel) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
