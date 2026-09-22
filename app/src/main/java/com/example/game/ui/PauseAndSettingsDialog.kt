package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.SaveManager
import com.example.game.engine.GameEngine

@Composable
fun PauseAndSettingsDialog(
    engine: GameEngine,
    saveManager: SaveManager,
    onResume: () -> Unit,
    onReturnToTitle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bgmVol by remember { mutableFloatStateOf(engine.audioEngine.bgmVolume) }
    var sfxVol by remember { mutableFloatStateOf(engine.audioEngine.sfxVolume) }
    var vibrationEnabled by remember { mutableStateOf(engine.vibrationManager.isEnabled) }
    var saveStatusMsg by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF141A24), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة التوقف والإعدادات",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onResume,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF263238), CircleShape)
                        .testTag("resume_game_x")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Sliders
            Text(text = "مستوى الموسيقى (BGM): ${(bgmVol * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            Slider(
                value = bgmVol,
                onValueChange = {
                    bgmVol = it
                    engine.audioEngine.bgmVolume = it
                },
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD700), activeTrackColor = Color(0xFF8B6B3D))
            )

            Text(text = "مؤثرات الصوت (SFX): ${(sfxVol * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            Slider(
                value = sfxVol,
                onValueChange = {
                    sfxVol = it
                    engine.audioEngine.sfxVolume = it
                },
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD700), activeTrackColor = Color(0xFF8B6B3D))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "الاهتزاز والتغذية اللمسية:", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = {
                        vibrationEnabled = it
                        engine.vibrationManager.isEnabled = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Slots Row
            Text(text = "حفظ اللعبة (اختر خانة الحفظ):", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slot in 1..3) {
                    Button(
                        onClick = {
                            saveManager.saveGame(
                                slotIndex = slot,
                                player = engine.player,
                                currentRegion = engine.currentRegionId,
                                quests = engine.quests,
                                openedChests = engine.openedChests,
                                defeatedBosses = engine.defeatedBosses,
                                unlockedWaypoints = engine.unlockedWaypoints
                            )
                            engine.audioEngine.playChest()
                            saveStatusMsg = "تم الحفظ بنجاح في الخانة $slot! ✓"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2633)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "خانة $slot", fontSize = 11.sp, color = Color(0xFFFFD700))
                    }
                }
            }

            if (saveStatusMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = saveStatusMsg, color = Color(0xFF76FF03), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resume & Exit Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "متابعة اللعب", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReturnToTitle,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "الشاشة الرئيسية", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
