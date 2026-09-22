package com.example.game.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.LoadedSaveData
import com.example.game.data.SaveManager
import com.example.game.model.Player
import com.example.game.model.PlayerClass

@Composable
fun TitleAndCharacterSelectScreen(
    saveManager: SaveManager,
    onStartNewGame: (PlayerClass) -> Unit,
    onLoadSave: (LoadedSaveData) -> Unit,
    modifier: Modifier = Modifier
) {
    var screenState by remember { mutableStateOf("TITLE") } // TITLE, SELECT_CLASS, LOAD_GAME
    var selectedClass by remember { mutableStateOf(PlayerClass.KNIGHT) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090A10),
                        Color(0xFF14081F),
                        Color(0xFF0B0D13)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (screenState) {
            "TITLE" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Banner
                    Text(
                        text = "⚔️ DARK REALM ⚔️",
                        color = Color(0xFFFFD700),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "عالم الظلال: ملحمة الفراغ الأبدي",
                        color = Color(0xFFBA68C8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Buttons
                    Button(
                        onClick = { screenState = "SELECT_CLASS" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6B3D)),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp)
                            .testTag("btn_new_game"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "رحلة جديدة (New Game)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { screenState = "LOAD_GAME" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2633)),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp)
                            .testTag("btn_load_game"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "متابعة الحفظ (Load Game)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "لعبة Action RPG أصلية بنظام تحكم كامل، معارك في الوقت الفعلي و 7 عوالم مترابطة",
                        color = Color(0xFF78909C),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            "SELECT_CLASS" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.9f)
                        .background(Color(0xFF141A24), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اختر فئة بطلك (Select Class)",
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Classes Choice Cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val classes = listOf(
                            Triple(PlayerClass.KNIGHT, "الفارس", "🗡️"),
                            Triple(PlayerClass.MAGE, "الساحر", "🔮"),
                            Triple(PlayerClass.ARCHER, "الرامي", "🏹")
                        )

                        for ((pClass, title, icon) in classes) {
                            val isSelected = selectedClass == pClass
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (isSelected) Color(0xFF263238) else Color(0xFF161C26),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color(0xFF37474F),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedClass = pClass }
                                    .padding(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = icon, fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = title,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pClass.description,
                                        color = Color(0xFFCFD8DC),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "HP: ${pClass.baseHp.toInt()} | MP: ${pClass.baseMana.toInt()}",
                                        color = Color(0xFF81C784),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { screenState = "TITLE" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            modifier = Modifier.weight(0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "رجوع")
                        }

                        Button(
                            onClick = { onStartNewGame(selectedClass) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6B3D)),
                            modifier = Modifier.weight(0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "بدء المغامرة الآن ⚔️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            "LOAD_GAME" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF141A24), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اختر ملف الحفظ (Load Game)",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    for (slot in 1..3) {
                        val summary = saveManager.getSlotSummary(slot)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF1E2633), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (summary.exists) {
                                        val data = saveManager.loadGame(slot)
                                        if (data != null) {
                                            onLoadSave(data)
                                        }
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "خانة الحفظ $slot",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (summary.exists) {
                                        Text(
                                            text = "${summary.playerClassName} | المستوى ${summary.level} | ${summary.gold} ذهب",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text(text = "فارغ (لا يوجد حفظ)", color = Color(0xFF78909C), fontSize = 11.sp)
                                    }
                                }

                                if (summary.exists) {
                                    Text(text = "تحميل ▶", color = Color(0xFF76FF03), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { screenState = "TITLE" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "رجوع")
                    }
                }
            }
        }
    }
}
