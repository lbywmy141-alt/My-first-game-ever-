package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.ItemType
import com.example.game.model.PlayerClass
import kotlin.math.*

@Composable
fun GameHud(
    engine: GameEngine,
    onOpenInventory: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val stats = player.stats
    val activeBoss = engine.activeBoss

    Box(modifier = modifier.fillMaxSize()) {
        // TOP BAR: Player Stats & Mini-map & Menu Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Player Status Bar (HP, MP, Stamina, XP)
            Column(
                modifier = Modifier
                    .background(Color(0xDD0D111A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .width(185.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Level Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF8B6B3D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stats.level}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = player.playerClass.displayName.split(" ")[0],
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // HP Bar
                BarIndicator(
                    label = "HP",
                    current = stats.currentHp,
                    max = stats.maxHp,
                    barColor = Color(0xFFE53935),
                    bgColor = Color(0xFF371818)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // MP Bar
                BarIndicator(
                    label = "MP",
                    current = stats.currentMana,
                    max = stats.maxMana,
                    barColor = Color(0xFF1E88E5),
                    bgColor = Color(0xFF102844)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Stamina Bar
                BarIndicator(
                    label = "ST",
                    current = stats.currentStamina,
                    max = stats.maxStamina,
                    barColor = Color(0xFF43A047),
                    bgColor = Color(0xFF143317)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // XP Bar
                val xpPct = (stats.xp.toFloat() / stats.xpToNext.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFF263238), RoundedCornerShape(1.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(xpPct)
                            .fillMaxHeight()
                            .background(Color(0xFFFFB300), RoundedCornerShape(1.dp))
                    )
                }
            }

            // Top-Center: Boss Bar (if active boss is present)
            if (activeBoss != null && activeBoss.currentHp > 0) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${activeBoss.type.displayName} (المرحلة ${activeBoss.currentPhase})",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val bossHpPct = (activeBoss.currentHp / activeBoss.type.maxHp).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .background(Color(0xFF260D0D), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bossHpPct)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFFD50000), Color(0xFFFF5252))),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            // Top-Right: Gold, Minimap, Menu Actions
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Gold Display
                    Row(
                        modifier = Modifier
                            .background(Color(0xDD0D111A), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.gold}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Bag / Inventory
                    HudIconButton(
                        icon = Icons.Default.Work,
                        onClick = onOpenInventory,
                        tag = "hud_bag_button"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Skill Tree
                    HudIconButton(
                        icon = Icons.Default.AutoFixHigh,
                        onClick = onOpenSkills,
                        tag = "hud_skills_button"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Map
                    HudIconButton(
                        icon = Icons.Default.Map,
                        onClick = onOpenMap,
                        tag = "hud_map_button"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Pause / Settings Menu
                    HudIconButton(
                        icon = Icons.Default.Menu,
                        onClick = onOpenMenu,
                        tag = "hud_menu_button"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mini Map Widget
                MiniMapRadar(engine = engine)
            }
        }

        // Active Quest Reminder (floating top-left beneath status bar)
        val activeQuest = engine.quests.firstOrNull { !it.isCompleted }
        if (activeQuest != null) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 120.dp)
                    .background(Color(0xBB0D111A), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0x66FFD700), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(max = 220.dp)
            ) {
                Column {
                    Text(
                        text = "📜 ${activeQuest.title}",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeQuest.description} (${activeQuest.currentCount}/${activeQuest.requiredCount})",
                        color = Color(0xFFE0E0E0),
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
            }
        }

        // Contextual Interaction Button (appears when near NPC / Chest / Portal)
        engine.nearbyInteractableText?.let { interactText ->
            Button(
                onClick = { engine.onInteract() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 80.dp)
                    .testTag("interact_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "✨ $interactText",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // BOTTOM CONTROLS: Joystick on Left, Combat Actions on Right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // VIRTUAL JOYSTICK
            VirtualJoystick(
                onMove = { vx, vy -> engine.onMoveInput(vx, vy) },
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // COMBAT ACTION CLUSTER
            CombatActionCluster(
                engine = engine,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun BarIndicator(label: String, current: Float, max: Float, barColor: Color, bgColor: Color) {
    val pct = (current / max).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.width(18.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(bgColor, RoundedCornerShape(2.dp))
                .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(2.dp))
            )
            Text(
                text = "${current.toInt()}/${max.toInt()}",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun HudIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tag: String) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Color(0xDD0D111A), CircleShape)
            .border(1.dp, Color(0xFF8B6B3D), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 17.dp),
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun MiniMapRadar(engine: GameEngine) {
    val map = engine.currentMap
    val p = engine.player

    Box(
        modifier = Modifier
            .size(76.dp)
            .background(Color(0xCC0D111A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Draw miniature radar dots
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val radarScaleX = size.width / map.pixelWidth
            val radarScaleY = size.height / map.pixelHeight

            // NPCs (Gold)
            for (npc in map.npcs) {
                drawCircle(Color(0xFFFFD700), radius = 2.5f, center = Offset(npc.x * radarScaleX, npc.y * radarScaleY))
            }
            // Portals (Cyan)
            for (portal in map.portals) {
                drawCircle(Color(0xFF00E5FF), radius = 3f, center = Offset(portal.x * radarScaleX, portal.y * radarScaleY))
            }
            // Enemies (Red)
            for (e in engine.enemies) {
                if (e.currentHp > 0) {
                    val c = if (e.isBoss) Color(0xFFFF1744) else Color(0xFFFF5252)
                    drawCircle(c, radius = if (e.isBoss) 4f else 2f, center = Offset(e.x * radarScaleX, e.y * radarScaleY))
                }
            }
            // Player (Green)
            drawCircle(Color(0xFF76FF03), radius = 3.5f, center = Offset(p.x * radarScaleX, p.y * radarScaleY))
        }
    }
}

@Composable
fun VirtualJoystick(onMove: (Float, Float) -> Unit, modifier: Modifier = Modifier) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 55f

    Box(
        modifier = modifier
            .size(130.dp)
            .background(Color(0x55000000), CircleShape)
            .border(2.dp, Color(0x77FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        thumbOffset = clamped
                        val normX = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                        val normY = (clamped.y / maxRadius).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > maxRadius) newOffset * (maxRadius / dist) else newOffset
                        thumbOffset = clamped
                        val normX = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                        val normY = (clamped.y / maxRadius).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .size(46.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF8B6B3D), Color(0xFF4A371E))),
                    CircleShape
                )
                .border(1.5.dp, Color(0xFFFFD700), CircleShape)
        )
    }
}

@Composable
fun CombatActionCluster(engine: GameEngine, modifier: Modifier = Modifier) {
    val player = engine.player
    val potionCount = player.inventory.filter { it.type == ItemType.POTION }.sumOf { it.quantity }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row: Quick Potion, Skill 1, Skill 2
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Quick Potion
            ActionButton(
                label = "🧪",
                subLabel = if (potionCount > 0) "$potionCount" else "",
                size = 46.dp,
                bgColor = Color(0xFF2E7D32),
                cooldownPct = (player.potionCooldown / 1.2f).coerceIn(0f, 1f),
                tag = "btn_potion",
                onClick = { engine.onQuickPotion() }
            )

            // Skill 1
            val skill1Name = when (player.playerClass) {
                PlayerClass.KNIGHT -> "إعصار"
                PlayerClass.MAGE -> "انفجار"
                PlayerClass.ARCHER -> "سهام 3x"
            }
            ActionButton(
                label = "⚡",
                subLabel = skill1Name,
                size = 46.dp,
                bgColor = Color(0xFF1565C0),
                cooldownPct = (player.skill1Cooldown / 4.0f).coerceIn(0f, 1f),
                tag = "btn_skill1",
                onClick = { engine.onSkill1() }
            )

            // Skill 2
            val skill2Name = when (player.playerClass) {
                PlayerClass.KNIGHT -> "صدمة"
                PlayerClass.MAGE -> "نيزك"
                PlayerClass.ARCHER -> "مطر"
            }
            ActionButton(
                label = "🔥",
                subLabel = skill2Name,
                size = 46.dp,
                bgColor = Color(0xFF6A1B9A),
                cooldownPct = (player.skill2Cooldown / 7.0f).coerceIn(0f, 1f),
                tag = "btn_skill2",
                onClick = { engine.onSkill2() }
            )
        }

        // Bottom row: Dodge, Block, Heavy Atk, Normal Attack
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dodge / Dash
            ActionButton(
                label = "💨",
                subLabel = "مراوغة",
                size = 50.dp,
                bgColor = Color(0xFF00838F),
                cooldownPct = (player.dodgeCooldown / 0.6f).coerceIn(0f, 1f),
                tag = "btn_dodge",
                onClick = { engine.onDodge() }
            )

            // Shield Block / Parry (Press to toggle or hold)
            val isBlocking = player.actionState == com.example.game.model.PlayerActionState.BLOCKING
            ActionButton(
                label = "🛡️",
                subLabel = if (isBlocking) "دفاع" else "صد",
                size = 50.dp,
                bgColor = if (isBlocking) Color(0xFF00E5FF) else Color(0xFF37474F),
                tag = "btn_block",
                onClick = { engine.onBlock(!isBlocking) }
            )

            // Heavy Attack
            ActionButton(
                label = "💥",
                subLabel = "ضربة قوية",
                size = 54.dp,
                bgColor = Color(0xFFD84315),
                tag = "btn_heavy_attack",
                onClick = { engine.onHeavyAttack() }
            )

            // Primary Attack (Large button with combo indicator)
            val comboText = if (player.comboStep > 0) "x${player.comboStep}" else ""
            ActionButton(
                label = "⚔️",
                subLabel = "هجوم $comboText",
                size = 72.dp,
                bgColor = Color(0xFFC62828),
                tag = "btn_normal_attack",
                onClick = { engine.onNormalAttack() }
            )
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    subLabel: String,
    size: androidx.compose.ui.unit.Dp,
    bgColor: Color,
    cooldownPct: Float = 0f,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(bgColor, CircleShape)
            .border(2.dp, Color(0xFFFFD700), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2f),
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = (size.value * 0.38f).sp)
            if (subLabel.isNotEmpty()) {
                Text(
                    text = subLabel,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Cooldown sweep overlay
        if (cooldownPct > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { cooldownPct },
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(size * 0.75f)
                )
            }
        }
    }
}
