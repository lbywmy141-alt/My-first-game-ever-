package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.art.PixelArtRenderer
import com.example.game.data.LoadedSaveData
import com.example.game.data.SaveManager
import com.example.game.engine.AudioEngine
import com.example.game.engine.GameEngine
import com.example.game.engine.ParticleType
import com.example.game.engine.VibrationManager
import com.example.game.model.*
import kotlinx.coroutines.isActive

enum class GameActiveScreen {
    TITLE,
    PLAYING,
    INVENTORY,
    SKILL_TREE,
    WORLD_MAP,
    QUEST_JOURNAL,
    PAUSE_MENU,
    SHOP,
    CRAFTING
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val audioEngine = remember { AudioEngine() }
    val vibrationManager = remember { VibrationManager(context) }
    val saveManager = remember { SaveManager(context) }
    val engine = remember { GameEngine(audioEngine, vibrationManager) }

    var currentScreen by remember { mutableStateOf(GameActiveScreen.TITLE) }
    var gameTicks by remember { mutableLongStateOf(0L) }

    // 60 FPS Game Loop
    LaunchedEffect(currentScreen) {
        var lastTime = System.nanoTime()
        while (isActive) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            if (currentScreen == GameActiveScreen.PLAYING) {
                engine.update(dt, 900f, 500f)
                gameTicks++
            }
            withFrameNanos { }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stopBgm()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (currentScreen) {
            GameActiveScreen.TITLE -> {
                TitleAndCharacterSelectScreen(
                    saveManager = saveManager,
                    onStartNewGame = { chosenClass ->
                        engine.initNewGame(chosenClass)
                        currentScreen = GameActiveScreen.PLAYING
                    },
                    onLoadSave = { loaded ->
                        engine.player = loaded.player
                        engine.openedChests.clear()
                        engine.openedChests.addAll(loaded.openedChests)
                        engine.defeatedBosses.clear()
                        engine.defeatedBosses.addAll(loaded.defeatedBosses)
                        engine.unlockedWaypoints.clear()
                        engine.unlockedWaypoints.addAll(loaded.unlockedWaypoints)
                        engine.loadRegion(loaded.currentRegion, loaded.player.x, loaded.player.y)
                        currentScreen = GameActiveScreen.PLAYING
                    }
                )
            }

            else -> {
                // The Main Canvas Game Viewport
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val viewW = size.width
                        val viewH = size.height
                        val tileSize = currentMapTileSize()
                        val map = engine.currentMap

                        val camX = engine.cameraX + engine.screenShakeOffsetX
                        val camY = engine.cameraY + engine.screenShakeOffsetY
                        val screenOriginX = viewW / 2f - camX
                        val screenOriginY = viewH / 2f - camY

                        // 1. Draw Visible Map Tiles
                        val startCol = ((-screenOriginX) / tileSize).toInt().coerceIn(0, map.widthTiles - 1)
                        val endCol = (((-screenOriginX) + viewW) / tileSize).toInt().coerceIn(0, map.widthTiles - 1)
                        val startRow = ((-screenOriginY) / tileSize).toInt().coerceIn(0, map.heightTiles - 1)
                        val endRow = (((-screenOriginY) + viewH) / tileSize).toInt().coerceIn(0, map.heightTiles - 1)

                        for (r in startRow..endRow) {
                            for (c in startCol..endCol) {
                                val tile = map.getTile(c, r)
                                val sx = screenOriginX + c * tileSize
                                val sy = screenOriginY + r * tileSize
                                PixelArtRenderer.drawTile(this, tile, sx, sy, tileSize)
                            }
                        }

                        // 2. Draw Portals
                        for (portal in map.portals) {
                            val px = screenOriginX + portal.x
                            val py = screenOriginY + portal.y
                            PixelArtRenderer.drawPortal(this, portal, px, py, engine.gameTimeHours)
                        }

                        // 3. Draw Chests
                        for (chest in map.chests) {
                            val cx = screenOriginX + chest.x
                            val cy = screenOriginY + chest.y
                            PixelArtRenderer.drawChest(this, chest, cx, cy)
                        }

                        // 4. Draw Breakables
                        for (prop in map.breakables) {
                            val bx = screenOriginX + prop.x
                            val by = screenOriginY + prop.y
                            PixelArtRenderer.drawBreakable(this, prop, bx, by)
                        }

                        // 5. Draw NPCs
                        for (npc in map.npcs) {
                            val nx = screenOriginX + npc.x
                            val ny = screenOriginY + npc.y
                            PixelArtRenderer.drawNpc(this, npc, nx, ny)
                        }

                        // 6. Draw Enemies
                        for (enemy in engine.enemies) {
                            val ex = screenOriginX + enemy.x
                            val ey = screenOriginY + enemy.y
                            PixelArtRenderer.drawEnemy(this, enemy, ex, ey)
                        }

                        // 7. Draw Player
                        val playerScreenX = screenOriginX + engine.player.x
                        val playerScreenY = screenOriginY + engine.player.y
                        PixelArtRenderer.drawPlayer(this, engine.player, playerScreenX, playerScreenY)

                        // 8. Draw Projectiles
                        for (p in engine.projectiles) {
                            val px = screenOriginX + p.x
                            val py = screenOriginY + p.y
                            drawCircle(color = p.color, radius = p.radius, center = Offset(px, py))
                        }

                        // 9. Draw Particle System
                        for (part in engine.particleSystem.particles) {
                            val px = screenOriginX + part.x
                            val py = screenOriginY + part.y
                            when (part.type) {
                                ParticleType.TEXT_DAMAGE -> {}
                                ParticleType.RAIN_DROP -> {
                                    drawLine(part.color, Offset(px, py), Offset(px - 5f, py + 14f), strokeWidth = 2f)
                                }
                                else -> {
                                    val alpha = (part.life / part.maxLife).coerceIn(0f, 1f)
                                    drawCircle(
                                        color = part.color.copy(alpha = alpha),
                                        radius = part.size,
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }

                        // 10. Draw Ambient Lighting & Lantern Glow
                        val isDungeon = engine.currentRegionId in listOf(
                            RegionId.MISTVEIL_MOUNTAINS,
                            RegionId.FROSTFANG_CAVERNS,
                            RegionId.ANCIENT_RUINS,
                            RegionId.VOID_CITADEL
                        )
                        PixelArtRenderer.drawAmbientLighting(
                            this,
                            engine.gameTimeHours,
                            playerScreenX,
                            playerScreenY,
                            isDungeon,
                            viewW,
                            viewH
                        )

                        // 11. Draw Floating Damage & Status Texts
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        for (d in engine.particleSystem.damageTexts) {
                            val dx = screenOriginX + d.x
                            val dy = screenOriginY + d.y
                            val alpha = (d.life / d.maxLife).coerceIn(0f, 1f)
                            paint.textSize = d.size * 2.2f
                            paint.color = android.graphics.Color.argb(
                                (alpha * 255).toInt(),
                                (d.color.red * 255).toInt(),
                                (d.color.green * 255).toInt(),
                                (d.color.blue * 255).toInt()
                            )
                            d.text?.let { txt ->
                                drawContext.canvas.nativeCanvas.drawText(txt, dx, dy, paint)
                            }
                        }
                    }

                    // Top HUD & Controls
                    GameHud(
                        engine = engine,
                        onOpenInventory = { currentScreen = GameActiveScreen.INVENTORY },
                        onOpenSkills = { currentScreen = GameActiveScreen.SKILL_TREE },
                        onOpenMap = { currentScreen = GameActiveScreen.WORLD_MAP },
                        onOpenMenu = { currentScreen = GameActiveScreen.PAUSE_MENU }
                    )

                    // NPC Dialogue Window (if active)
                    engine.activeDialogueNode?.let { node ->
                        NpcDialogueDialog(
                            engine = engine,
                            node = node,
                            onClose = { engine.activeDialogueNode = null }
                        )
                    }

                    // Shop Window (if triggered from NPC)
                    if (engine.isShopOpen) {
                        ShopDialog(
                            engine = engine,
                            onClose = { engine.isShopOpen = false }
                        )
                    }

                    // Crafting Window (if triggered from NPC)
                    if (engine.isCraftingOpen) {
                        CraftingDialog(
                            engine = engine,
                            onClose = { engine.isCraftingOpen = false }
                        )
                    }

                    // Sub-Screens Overlays
                    when (currentScreen) {
                        GameActiveScreen.INVENTORY -> {
                            InventoryScreen(
                                engine = engine,
                                onClose = { currentScreen = GameActiveScreen.PLAYING }
                            )
                        }
                        GameActiveScreen.SKILL_TREE -> {
                            SkillTreeScreen(
                                engine = engine,
                                onClose = { currentScreen = GameActiveScreen.PLAYING }
                            )
                        }
                        GameActiveScreen.WORLD_MAP -> {
                            WorldMapScreen(
                                engine = engine,
                                onClose = { currentScreen = GameActiveScreen.PLAYING }
                            )
                        }
                        GameActiveScreen.PAUSE_MENU -> {
                            PauseAndSettingsDialog(
                                engine = engine,
                                saveManager = saveManager,
                                onResume = { currentScreen = GameActiveScreen.PLAYING },
                                onReturnToTitle = { currentScreen = GameActiveScreen.TITLE }
                            )
                        }
                        else -> {}
                    }

                    // Game Over / You Died Overlay
                    if (engine.player.actionState == PlayerActionState.DEAD) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xEE090305)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "لقد هلكت",
                                    color = Color(0xFFFF1744),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 4.sp
                                )
                                Text(
                                    text = "ابتلعتك ظلال الفراغ الأبدي...",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                )

                                Button(
                                    onClick = { engine.respawnPlayer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6B3D)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(text = "العودة للحياة في القرية 🛡️", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun currentMapTileSize(): Float = 64f
