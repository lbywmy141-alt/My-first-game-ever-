package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.game.engine.GameEngine
import com.example.game.model.QuestCategory

@Composable
fun QuestJournalScreen(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("MAIN") }

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
                Text(
                    text = "سجل المهام والرحلات",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF263238), CircleShape)
                        .testTag("close_journal")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = "MAIN" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "MAIN") Color(0xFF8B6B3D) else Color(0xFF1E2633)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "المهام الرئيسية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedTab = "SIDE" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "SIDE") Color(0xFF8B6B3D) else Color(0xFF1E2633)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "المهام الجانبية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quests List
            val filteredQuests = engine.quests.filter {
                if (selectedTab == "MAIN") it.category == QuestCategory.MAIN else it.category != QuestCategory.MAIN
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredQuests) { quest ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141A24), RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (quest.isCompleted) Color(0xFF76FF03) else Color(0xFF37474F),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quest.title,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (quest.isClaimed) "مكتملة ومستلمة ✓" else if (quest.isCompleted) "جاهزة للاستلام ✨" else "${quest.currentCount}/${quest.requiredCount}",
                                    color = if (quest.isCompleted) Color(0xFF76FF03) else Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = quest.description,
                                color = Color(0xFFCFD8DC),
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المكافأة: ${quest.rewardGold} ذهب  |  ${quest.rewardXp} XP ${quest.rewardItem?.let { " | ${it.name}" } ?: ""}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp
                                )

                                if (quest.isCompleted && !quest.isClaimed) {
                                    Button(
                                        onClick = { engine.claimQuestReward(quest) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(text = "استلام المكافأة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
