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
import com.example.game.model.PlayerClass

data class SkillNode(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val cost: Int,
    val requiredLevel: Int,
    val prerequisiteId: String? = null
)

@Composable
fun SkillTreeScreen(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val stats = player.stats

    // Class-specific skill catalog
    val classSkills = remember(player.playerClass) {
        when (player.playerClass) {
            PlayerClass.KNIGHT -> listOf(
                SkillNode("whirlwind", "زوبعة النصل", "هجوم دائري 360 درجة يصيب جميع الأعداء المحيطين بك بضرر فادح.", "🌪️", 1, 1),
                SkillNode("shield_bash", "صدمة الترس", "اندفاع قوي نحو الأمام يصدم الأعداء ويشل حركتهم لمدة قصيرة.", "🛡️", 1, 2, "whirlwind"),
                SkillNode("iron_will", "الإرادة الفولاذية", "مهارة سلبية تزيد من إجمالي نقاط الدفاع والحيوية بنسبة 25%.", "💪", 2, 4, "shield_bash"),
                SkillNode("berserk_frenzy", "غضب الفارس البربري", "مهارة سلبية تزيد ضرر الضربات القاضية وسرعة الهجوم عند انخفاض الصحة.", "🔥", 3, 6, "iron_will")
            )
            PlayerClass.MAGE -> listOf(
                SkillNode("arcane_burst", "الانفجار الأثيري", "يطلق 3 مقذوفات سحرية متتبعة تبحث عن الأعداء وتنفجر عند الاصطدام.", "✨", 1, 1),
                SkillNode("meteor_flame", "نيزك الجحيم", "استدعاء نيزك ملتهب من السماء يحدث انفجاراً هائلاً في نقطة الهدف.", "☄️", 1, 2, "arcane_burst"),
                SkillNode("mana_shield", "درع الطاقة الأثيرية", "مهارة سلبية تمتص 30% من الضرر الوارد وتستهلك المانا بدلاً من الصحة.", "🔮", 2, 4, "meteor_flame"),
                SkillNode("void_annihilation", "إبادة الفراغ", "إطلاق دوامة سوداء تسحب الأعداء وتلحق بهم ضرراً سحرياً مستمراً.", "🌌", 3, 6, "mana_shield")
            )
            PlayerClass.ARCHER -> listOf(
                SkillNode("triple_shot", "الرمية الثلاثية", "إطلاق 3 سهام خارقة متزامنة في مخروط واسع تخترق صفوف الأعداء.", "🏹", 1, 1),
                SkillNode("rain_of_arrows", "وابل السهام القاتلة", "إمطار منطقة كاملة بسهام حادة تسقط بسرعة وتسحق الوحوش.", "🌧️", 1, 2, "triple_shot"),
                SkillNode("eagle_eye", "عين الصقر", "مهارة سلبية تزيد من فرصة الضربة القاضية ونطاق الرؤية بنسبة 20%.", "🦅", 2, 4, "rain_of_arrows"),
                SkillNode("shadow_step", "خطوة الظلال", "اندفاع شبحي يمنحك سرعة مضاعفة واختفاء مؤقت عن أعين الأعداء.", "👤", 3, 6, "eagle_eye")
            )
        }
    }

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
                        text = "شجرة مهارات ${player.playerClass.displayName}",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "نقاط المهارة المتاحة: ${stats.skillPoints}",
                        color = Color(0xFF76FF03),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF263238), CircleShape)
                        .testTag("close_skills")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skills List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(classSkills) { skill ->
                    val isUnlocked = player.unlockedSkills.contains(skill.id) || skill.requiredLevel == 1
                    val meetsLevel = stats.level >= skill.requiredLevel
                    val meetsPrereq = skill.prerequisiteId == null || player.unlockedSkills.contains(skill.prerequisiteId)
                    val canUnlock = !isUnlocked && meetsLevel && meetsPrereq && stats.skillPoints >= skill.cost

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141A24), RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (isUnlocked) Color(0xFFFFD700) else Color(0xFF37474F),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skill Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(if (isUnlocked) Color(0xFF263238) else Color(0xFF1B1F28), CircleShape)
                                .border(1.5.dp, if (isUnlocked) Color(0xFFFFD700) else Color(0xFF455A64), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = skill.icon, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = skill.name,
                                    color = if (isUnlocked) Color(0xFFFFD700) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "المستوى ${skill.requiredLevel}",
                                    color = if (meetsLevel) Color(0xFF81C784) else Color(0xFFFF5252),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = skill.description,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Unlock button / Status
                        if (isUnlocked) {
                            Text(
                                text = "✓ مفعّلة",
                                color = Color(0xFF76FF03),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (canUnlock) {
                                        stats.skillPoints -= skill.cost
                                        player.unlockedSkills.add(skill.id)
                                        engine.audioEngine.playLevelUp()
                                    }
                                },
                                enabled = canUnlock,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF8F00),
                                    disabledContainerColor = Color(0xFF37474F)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "تفعيل (${skill.cost})",
                                    color = if (canUnlock) Color.Black else Color(0xFF90A4AE),
                                    fontSize = 11.sp,
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
