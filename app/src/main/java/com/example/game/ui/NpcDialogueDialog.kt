package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.DialogueDatabase
import com.example.game.model.DialogueNode

@Composable
fun NpcDialogueDialog(
    engine: GameEngine,
    node: DialogueNode,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFA141A24), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Speaker Name & Portrait icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF263238), CircleShape)
                        .border(1.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧙‍♂️", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = node.speakerName,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dialogue Text
            Text(
                text = node.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selectable Dialogue Options
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (option in node.options) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2633), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(6.dp))
                            .clickable {
                                engine.audioEngine.playUiClick()
                                when (option.action) {
                                    "open_shop" -> engine.isShopOpen = true
                                    "open_craft" -> engine.isCraftingOpen = true
                                    "accept_quest_talk_valerie" -> engine.updateQuestProgress("talk_valerie")
                                    "talk_brom_quest" -> engine.updateQuestProgress("talk_brom")
                                    else -> {}
                                }
                                if (option.nextNodeId != null && option.nextNodeId != "close") {
                                    val nextNode = DialogueDatabase.getNode(node.npcId, option.nextNodeId)
                                    engine.activeDialogueNode = nextNode
                                } else {
                                    onClose()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "💬 ${option.text}",
                            color = Color(0xFFE0E0E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
