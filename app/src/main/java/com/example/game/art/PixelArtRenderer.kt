package com.example.game.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.*
import kotlin.math.cos
import kotlin.math.sin

object PixelArtRenderer {

    // TILES RENDERING
    fun drawTile(drawScope: DrawScope, tile: TileType, x: Float, y: Float, size: Float) {
        val baseColor = Color(tile.colorHex)
        drawScope.drawRect(color = baseColor, topLeft = Offset(x, y), size = Size(size, size))

        when (tile) {
            TileType.GRASS -> {
                // Grass blades and small flowers
                drawScope.drawRect(Color(0xFF2E5333), Offset(x + 8f, y + 10f), Size(4f, 8f))
                drawScope.drawRect(Color(0xFF43754A), Offset(x + 24f, y + 36f), Size(4f, 10f))
                drawScope.drawRect(Color(0xFF2E5333), Offset(x + 48f, y + 20f), Size(4f, 6f))
                // flower dot
                if ((x.toInt() + y.toInt()) % 5 == 0) {
                    drawScope.drawCircle(Color(0xFFFFEB3B), radius = 3f, center = Offset(x + 36f, y + 16f))
                }
            }
            TileType.COBBLESTONE -> {
                // Stone brick outlines
                val mortar = Color(0xFF4E4E4E)
                val highlight = Color(0xFF8A8A8A)
                drawScope.drawRect(mortar, Offset(x, y), Size(size, 2f))
                drawScope.drawRect(mortar, Offset(x, y + size / 2f), Size(size, 2f))
                drawScope.drawRect(mortar, Offset(x + size / 2f, y), Size(2f, size / 2f))
                drawScope.drawRect(mortar, Offset(x + size / 4f, y + size / 2f), Size(2f, size / 2f))
                // stone highlight
                drawScope.drawRect(highlight, Offset(x + 4f, y + 4f), Size(size / 2f - 8f, 3f))
            }
            TileType.STONE_WALL, TileType.RUIN_PILLAR -> {
                // Stone blocks with shadow
                drawScope.drawRect(Color(0xFF2B2B2B), Offset(x, y + size - 8f), Size(size, 8f))
                drawScope.drawRect(Color(0xFF616161), Offset(x, y), Size(size, 4f))
                drawScope.drawRect(Color(0xFF1E1E1E), Offset(x + size / 2f, y), Size(3f, size))
            }
            TileType.WATER -> {
                // Animated water ripples
                drawScope.drawRect(Color(0xFF42A5F5), Offset(x + 12f, y + 16f), Size(18f, 4f))
                drawScope.drawRect(Color(0xFF90CAF9), Offset(x + 32f, y + 40f), Size(14f, 3f))
            }
            TileType.SWAMP_WATER -> {
                // Murky swamp with green scum
                drawScope.drawRect(Color(0xFF33691E), Offset(x + 8f, y + 14f), Size(16f, 6f))
                drawScope.drawCircle(Color(0xFF558B2F), radius = 5f, center = Offset(x + 38f, y + 36f))
            }
            TileType.ICE, TileType.SNOW -> {
                // Glistening ice cracks
                drawScope.drawLine(Color(0xFFFFFFFF), Offset(x + 6f, y + 10f), Offset(x + 28f, y + 32f), strokeWidth = 2f)
                drawScope.drawLine(Color(0xFFFFFFFF), Offset(x + 28f, y + 32f), Offset(x + 46f, y + 26f), strokeWidth = 1.5f)
            }
            TileType.VOID_STONE, TileType.VOID_WALL -> {
                // Pulsing dark void runes & cracks
                drawScope.drawRect(Color(0xFF9C27B0), Offset(x + 14f, y + 20f), Size(8f, 3f))
                drawScope.drawRect(Color(0xFFBA68C8), Offset(x + 36f, y + 42f), Size(6f, 2f))
            }
            TileType.TREE_TRUNK -> {
                // Wood bark texture
                drawScope.drawRect(Color(0xFF3E2723), Offset(x + 8f, y), Size(4f, size))
                drawScope.drawRect(Color(0xFF2D1810), Offset(x + 24f, y), Size(5f, size))
            }
            else -> {}
        }
    }

    // PLAYER RENDERING
    fun drawPlayer(drawScope: DrawScope, player: Player, screenX: Float, screenY: Float) {
        val dir = player.direction
        val walkOffset = if (player.actionState == PlayerActionState.RUNNING) {
            sin(player.animTimer * 25f) * 4f
        } else 0f

        // Shadow under player
        drawScope.drawOval(
            color = Color(0x66000000),
            topLeft = Offset(screenX - 16f, screenY + 14f),
            size = Size(32f, 12f)
        )

        val isFlipped = dir == Direction.LEFT

        when (player.playerClass) {
            PlayerClass.KNIGHT -> drawKnight(drawScope, screenX, screenY, dir, walkOffset, player.actionState, isFlipped)
            PlayerClass.MAGE -> drawMage(drawScope, screenX, screenY, dir, walkOffset, player.actionState, isFlipped)
            PlayerClass.ARCHER -> drawArcher(drawScope, screenX, screenY, dir, walkOffset, player.actionState, isFlipped)
        }

        // Action specific overlays (e.g. Blocking shield glow)
        if (player.actionState == PlayerActionState.BLOCKING) {
            drawScope.drawCircle(
                color = if (player.isParrying) Color(0xAA00E5FF) else Color(0x7790CAF9),
                radius = 24f,
                center = Offset(screenX, screenY),
                style = Stroke(width = 3.5f)
            )
        }
    }

    private fun drawKnight(drawScope: DrawScope, x: Float, y: Float, dir: Direction, walkOffset: Float, state: PlayerActionState, flip: Boolean) {
        // Legs
        drawScope.drawRect(Color(0xFF424242), Offset(x - 8f, y + 10f + walkOffset), Size(6f, 12f))
        drawScope.drawRect(Color(0xFF424242), Offset(x + 2f, y + 10f - walkOffset), Size(6f, 12f))

        // Silver Chest Armor
        drawScope.drawRect(Color(0xFFB0BEC5), Offset(x - 12f, y - 6f), Size(24f, 18f))
        drawScope.drawRect(Color(0xFFCFD8DC), Offset(x - 10f, y - 4f), Size(20f, 6f)) // highlight
        drawScope.drawRect(Color(0xFFFFD700), Offset(x - 3f, y - 2f), Size(6f, 8f)) // Gold Crest

        // Blue Cape
        val capeShift = if (flip) 8f else -8f
        drawScope.drawRect(Color(0xFF1565C0), Offset(x + capeShift - 4f, y - 2f), Size(8f, 20f))

        // Steel Helmet
        drawScope.drawRect(Color(0xFF78909C), Offset(x - 10f, y - 22f), Size(20f, 16f))
        drawScope.drawRect(Color(0xFFCFD8DC), Offset(x - 8f, y - 20f), Size(16f, 5f))
        // Visor slit
        drawScope.drawRect(Color(0xFF1A237E), Offset(x - 7f, y - 13f), Size(14f, 4f))
        // Red Helmet Plume
        drawScope.drawRect(Color(0xFFD32F2F), Offset(x - 4f, y - 28f), Size(8f, 7f))

        // Weapon (Sword) & Shield
        val weaponX = if (flip) x - 18f else x + 14f
        val swordY = if (state.name.contains("ATTACK")) y - 14f else y - 4f
        // Steel Sword
        drawScope.drawRect(Color(0xFFECEFF1), Offset(weaponX, swordY), Size(4f, 22f))
        drawScope.drawRect(Color(0xFFFFB300), Offset(weaponX - 3f, swordY + 16f), Size(10f, 4f)) // hilt

        // Heater Shield
        val shieldX = if (flip) x + 8f else x - 16f
        drawScope.drawRect(Color(0xFF1E88E5), Offset(shieldX, y - 2f), Size(10f, 16f))
        drawScope.drawRect(Color(0xFFFFC107), Offset(shieldX + 2f, y), Size(6f, 12f))
    }

    private fun drawMage(drawScope: DrawScope, x: Float, y: Float, dir: Direction, walkOffset: Float, state: PlayerActionState, flip: Boolean) {
        // Feet
        drawScope.drawRect(Color(0xFF3E2723), Offset(x - 7f, y + 12f + walkOffset), Size(5f, 8f))
        drawScope.drawRect(Color(0xFF3E2723), Offset(x + 2f, y + 12f - walkOffset), Size(5f, 8f))

        // Arcane Robes (Deep violet & gold)
        drawScope.drawRect(Color(0xFF4A148C), Offset(x - 12f, y - 6f), Size(24f, 20f))
        drawScope.drawRect(Color(0xFFFFD54F), Offset(x - 2f, y - 6f), Size(4f, 20f)) // Gold trim robe

        // Wizard Hood
        drawScope.drawRect(Color(0xFF6A1B9A), Offset(x - 10f, y - 22f), Size(20f, 16f))
        // Shadow in hood & glowing eyes
        drawScope.drawRect(Color(0xFF1A0033), Offset(x - 8f, y - 14f), Size(16f, 8f))
        drawScope.drawCircle(Color(0xFF00E5FF), radius = 2f, center = Offset(x - 3f, y - 10f))
        drawScope.drawCircle(Color(0xFF00E5FF), radius = 2f, center = Offset(x + 3f, y - 10f))

        // Arcane Staff
        val staffX = if (flip) x - 18f else x + 16f
        drawScope.drawRect(Color(0xFF5D4037), Offset(staffX, y - 18f), Size(4f, 32f))
        // Glowing magic crystal at tip
        drawScope.drawCircle(Color(0xFF00E5FF), radius = 6f, center = Offset(staffX + 2f, y - 20f))
        drawScope.drawCircle(Color(0xFFE0F7FA), radius = 3f, center = Offset(staffX + 2f, y - 20f))
    }

    private fun drawArcher(drawScope: DrawScope, x: Float, y: Float, dir: Direction, walkOffset: Float, state: PlayerActionState, flip: Boolean) {
        // Boots
        drawScope.drawRect(Color(0xFF4E342E), Offset(x - 7f, y + 11f + walkOffset), Size(5f, 10f))
        drawScope.drawRect(Color(0xFF4E342E), Offset(x + 2f, y + 11f - walkOffset), Size(5f, 10f))

        // Forest Green Hunter Tunic
        drawScope.drawRect(Color(0xFF2E7D32), Offset(x - 10f, y - 6f), Size(20f, 18f))
        drawScope.drawRect(Color(0xFF1B5E20), Offset(x - 9f, y - 4f), Size(18f, 5f))
        // Leather Belt
        drawScope.drawRect(Color(0xFF5D4037), Offset(x - 10f, y + 5f), Size(20f, 3f))

        // Archer Cowl & Face
        drawScope.drawRect(Color(0xFF388E3C), Offset(x - 9f, y - 22f), Size(18f, 16f))
        drawScope.drawRect(Color(0xFFFFCC80), Offset(x - 7f, y - 14f), Size(14f, 7f)) // Face
        drawScope.drawCircle(Color(0xFF1B5E20), radius = 1.8f, center = Offset(x - 2f, y - 11f)) // Eye
        drawScope.drawCircle(Color(0xFF1B5E20), radius = 1.8f, center = Offset(x + 3f, y - 11f))

        // Recurve Bow
        val bowX = if (flip) x - 16f else x + 14f
        drawScope.drawLine(Color(0xFF8D6E63), Offset(bowX, y - 18f), Offset(bowX + 6f, y + 2f), strokeWidth = 3f)
        drawScope.drawLine(Color(0xFF8D6E63), Offset(bowX + 6f, y + 2f), Offset(bowX, y + 22f), strokeWidth = 3f)
        // Bowstring
        drawScope.drawLine(Color(0xFFE0E0E0), Offset(bowX, y - 18f), Offset(bowX, y + 22f), strokeWidth = 1f)
    }

    // ENEMY RENDERING
    fun drawEnemy(drawScope: DrawScope, enemy: Enemy, screenX: Float, screenY: Float) {
        val isFlipped = enemy.direction == Direction.LEFT

        // Shadow
        val shadowWidth = if (enemy.isBoss) 64f else 28f
        drawScope.drawOval(
            color = Color(0x55000000),
            topLeft = Offset(screenX - shadowWidth / 2f, screenY + (if (enemy.isBoss) 30f else 12f)),
            size = Size(shadowWidth, 12f)
        )

        // Telegraph Attack Arc
        if (enemy.aiState == EnemyAiState.TELEGRAPH_ATTACK) {
            val telegraphColor = Color(0x66FF1744)
            val ringRadius = if (enemy.isBoss) 110f else enemy.type.attackRange + 10f
            drawScope.drawCircle(
                color = telegraphColor,
                radius = ringRadius,
                center = Offset(screenX, screenY),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
        }

        // Draw Specific Monster Sprites
        when (enemy.type) {
            EnemyType.GOBLIN_SCOUT -> drawGoblin(drawScope, screenX, screenY, enemy, isFlipped)
            EnemyType.SHADOW_WOLF -> drawShadowWolf(drawScope, screenX, screenY, enemy, isFlipped)
            EnemyType.SKELETON_WARRIOR -> drawSkeleton(drawScope, screenX, screenY, enemy, isFlipped)
            EnemyType.VENOM_SPIDER -> drawSpider(drawScope, screenX, screenY, enemy)
            EnemyType.CRYPT_ZOMBIE -> drawZombie(drawScope, screenX, screenY, enemy)
            EnemyType.OUTLAW_BANDIT -> drawBandit(drawScope, screenX, screenY, enemy, isFlipped)
            EnemyType.DARK_KNIGHT -> drawDarkKnight(drawScope, screenX, screenY, enemy, isFlipped)
            EnemyType.VOID_CULTIST -> drawVoidCultist(drawScope, screenX, screenY, enemy)
            // Bosses
            EnemyType.ALPHA_BEHEMOTH -> drawAlphaBehemoth(drawScope, screenX, screenY, enemy)
            EnemyType.VOIDWRAITH_MORVATH -> drawVoidwraith(drawScope, screenX, screenY, enemy)
            EnemyType.LORD_MALAKOR -> drawLordMalakor(drawScope, screenX, screenY, enemy, isFlipped)
        }

        // Overhead Health Bar (for non-bosses, or mini-bosses in field)
        if (!enemy.isBoss && enemy.currentHp < enemy.type.maxHp && enemy.currentHp > 0) {
            val barW = 36f
            val barH = 5f
            val barX = screenX - barW / 2f
            val barY = screenY - 32f
            drawScope.drawRect(Color(0xFF212121), Offset(barX, barY), Size(barW, barH))
            val hpPct = (enemy.currentHp / enemy.type.maxHp).coerceIn(0f, 1f)
            drawScope.drawRect(Color(0xFFFF1744), Offset(barX, barY), Size(barW * hpPct, barH))
        }
    }

    private fun drawGoblin(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Small green creature
        drawScope.drawRect(Color(0xFF558B2F), Offset(x - 8f, y - 6f), Size(16f, 14f)) // Body
        drawScope.drawRect(Color(0xFF33691E), Offset(x - 7f, y - 18f), Size(14f, 12f)) // Head
        // Pointed ears
        drawScope.drawRect(Color(0xFF558B2F), Offset(x - 12f, y - 16f), Size(5f, 4f))
        drawScope.drawRect(Color(0xFF558B2F), Offset(x + 7f, y - 16f), Size(5f, 4f))
        // Red eyes
        drawScope.drawCircle(Color.Red, 1.8f, Offset(x - 3f, y - 13f))
        drawScope.drawCircle(Color.Red, 1.8f, Offset(x + 3f, y - 13f))
        // Spiked Club
        val clubX = if (flip) x - 14f else x + 10f
        drawScope.drawRect(Color(0xFF4E342E), Offset(clubX, y - 10f), Size(5f, 18f))
    }

    private fun drawShadowWolf(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Dark wolf quad
        drawScope.drawOval(Color(0xFF212121), Offset(x - 18f, y - 8f), Size(36f, 18f)) // body
        val headX = if (flip) x - 22f else x + 10f
        drawScope.drawRect(Color(0xFF1E1E1E), Offset(headX, y - 16f), Size(12f, 14f)) // head
        // Pointy ears
        drawScope.drawRect(Color(0xFF37474F), Offset(headX + 2f, y - 20f), Size(4f, 5f))
        // Cyan glowing eyes
        drawScope.drawCircle(Color(0xFF00E5FF), 2f, Offset(headX + 5f, y - 11f))
        // Legs
        drawScope.drawRect(Color(0xFF121212), Offset(x - 14f, y + 8f), Size(4f, 10f))
        drawScope.drawRect(Color(0xFF121212), Offset(x + 10f, y + 8f), Size(4f, 10f))
    }

    private fun drawSkeleton(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Ribcage
        drawScope.drawRect(Color(0xFFE0E0E0), Offset(x - 7f, y - 4f), Size(14f, 14f))
        drawScope.drawRect(Color(0xFF424242), Offset(x - 5f, y), Size(10f, 2f)) // rib spaces
        // Skull
        drawScope.drawRect(Color(0xFFEEEEEE), Offset(x - 8f, y - 18f), Size(16f, 14f))
        drawScope.drawCircle(Color.Black, 2.5f, Offset(x - 3f, y - 11f))
        drawScope.drawCircle(Color.Black, 2.5f, Offset(x + 3f, y - 11f))
        // Rusty Sword
        val swordX = if (flip) x - 16f else x + 12f
        drawScope.drawRect(Color(0xFF8D6E63), Offset(swordX, y - 10f), Size(4f, 20f))
    }

    private fun drawSpider(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy) {
        // Spider body & venom abdomen
        drawScope.drawCircle(Color(0xFF311B92), 10f, Offset(x, y - 4f)) // Abdomen
        drawScope.drawCircle(Color(0xFF4A148C), 7f, Offset(x, y + 6f)) // Head
        drawScope.drawCircle(Color(0xFF76FF03), 1.5f, Offset(x - 2f, y + 7f)) // Glowing green eye
        drawScope.drawCircle(Color(0xFF76FF03), 1.5f, Offset(x + 2f, y + 7f))
        // 8 legs
        for (i in -2..2) {
            if (i == 0) continue
            val lx = i * 6f
            drawScope.drawLine(Color(0xFF212121), Offset(x, y), Offset(x + lx * 2.2f, y + 8f), strokeWidth = 2.5f)
            drawScope.drawLine(Color(0xFF212121), Offset(x, y), Offset(x + lx * 2.2f, y - 8f), strokeWidth = 2.5f)
        }
    }

    private fun drawZombie(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy) {
        // Tattered rotting body
        drawScope.drawRect(Color(0xFF33691E), Offset(x - 9f, y - 6f), Size(18f, 18f))
        drawScope.drawRect(Color(0xFF558B2F), Offset(x - 8f, y - 20f), Size(16f, 14f)) // head
        drawScope.drawCircle(Color(0xFFE0E0E0), 2f, Offset(x - 3f, y - 13f))
        drawScope.drawCircle(Color(0xFFE0E0E0), 2f, Offset(x + 3f, y - 13f))
        // Outstretched rotting arms
        drawScope.drawRect(Color(0xFF558B2F), Offset(x - 14f, y - 2f), Size(28f, 5f))
    }

    private fun drawBandit(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Hooded rogue
        drawScope.drawRect(Color(0xFF37474F), Offset(x - 10f, y - 6f), Size(20f, 18f))
        drawScope.drawRect(Color(0xFF263238), Offset(x - 8f, y - 20f), Size(16f, 14f)) // hood
        // Red mask
        drawScope.drawRect(Color(0xFFC62828), Offset(x - 6f, y - 11f), Size(12f, 5f))
        // Dual daggers
        val d1X = if (flip) x - 14f else x + 12f
        drawScope.drawRect(Color(0xFFCFD8DC), Offset(d1X, y - 2f), Size(3f, 12f))
    }

    private fun drawDarkKnight(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Spiked black plate armor
        drawScope.drawRect(Color(0xFF212121), Offset(x - 14f, y - 8f), Size(28f, 22f))
        drawScope.drawRect(Color(0xFFB71C1C), Offset(x - 12f, y - 6f), Size(24f, 4f)) // Crimson belt
        // Horned helm
        drawScope.drawRect(Color(0xFF1B1B1B), Offset(x - 11f, y - 26f), Size(22f, 18f))
        drawScope.drawRect(Color(0xFFD50000), Offset(x - 8f, y - 18f), Size(16f, 4f)) // Glowing red visor
        // Giant Greatsword
        val swordX = if (flip) x - 22f else x + 16f
        drawScope.drawRect(Color(0xFF37474F), Offset(swordX, y - 24f), Size(6f, 38f))
        drawScope.drawRect(Color(0xFFB71C1C), Offset(swordX + 1f, y - 20f), Size(4f, 30f)) // Crimson blade glow
    }

    private fun drawVoidCultist(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy) {
        // Floating dark cultist
        val floatOffset = sin(enemy.animTimer * 6f) * 4f
        drawScope.drawRect(Color(0xFF120024), Offset(x - 11f, y - 10f + floatOffset), Size(22f, 24f))
        drawScope.drawRect(Color(0xFF311B92), Offset(x - 9f, y - 24f + floatOffset), Size(18f, 16f)) // hood
        // Void rune aura
        drawScope.drawCircle(Color(0xFFBA68C8), 5f, Offset(x, y - 8f + floatOffset))
    }

    // BOSS RENDERING
    private fun drawAlphaBehemoth(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy) {
        // Massive horned swamp monster
        val enragedTint = if (enemy.isEnraged) Color(0xFFFF5722) else Color(0xFF33691E)
        drawScope.drawOval(enragedTint, Offset(x - 34f, y - 20f), Size(68f, 44f)) // Big torso
        drawScope.drawOval(Color(0xFF1B5E20), Offset(x - 24f, y - 38f), Size(48f, 32f)) // Head
        // Horns
        drawScope.drawRect(Color(0xFFD7CCC8), Offset(x - 32f, y - 50f), Size(10f, 18f))
        drawScope.drawRect(Color(0xFFD7CCC8), Offset(x + 22f, y - 50f), Size(10f, 18f))
        // Glowing red eyes
        drawScope.drawCircle(Color.Red, 4f, Offset(x - 10f, y - 28f))
        drawScope.drawCircle(Color.Red, 4f, Offset(x + 10f, y - 28f))
        // Spiked fists
        drawScope.drawRect(Color(0xFF4E342E), Offset(x - 42f, y - 4f), Size(14f, 26f))
        drawScope.drawRect(Color(0xFF4E342E), Offset(x + 28f, y - 4f), Size(14f, 26f))
    }

    private fun drawVoidwraith(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy) {
        // Spectral floating wraith with glowing skull
        val floatOffset = sin(enemy.animTimer * 5f) * 6f
        drawScope.drawOval(Color(0xBB311B92), Offset(x - 26f, y - 24f + floatOffset), Size(52f, 58f))
        // Glowing void core
        drawScope.drawCircle(Color(0xFF00E5FF), 10f, Offset(x, y + floatOffset))
        // Wraith hood and skull
        drawScope.drawRect(Color(0xFF0A0014), Offset(x - 14f, y - 42f + floatOffset), Size(28f, 22f))
        drawScope.drawCircle(Color(0xFF80D8FF), 3f, Offset(x - 6f, y - 32f + floatOffset))
        drawScope.drawCircle(Color(0xFF80D8FF), 3f, Offset(x + 6f, y - 32f + floatOffset))
        // Scythe
        drawScope.drawLine(Color(0xFF5D4037), Offset(x + 24f, y - 48f + floatOffset), Offset(x + 24f, y + 24f + floatOffset), strokeWidth = 5f)
        drawScope.drawArc(Color(0xFFE040FB), 180f, 140f, true, Offset(x + 8f, y - 64f + floatOffset), Size(38f, 38f))
    }

    private fun drawLordMalakor(drawScope: DrawScope, x: Float, y: Float, enemy: Enemy, flip: Boolean) {
        // Towering Void King (3 phases)
        val auraColor = when (enemy.currentPhase) {
            1 -> Color(0x667B1FA2)
            2 -> Color(0x99D500F9)
            else -> Color(0xCCFF1744) // Frenzy phase
        }

        // Void Wings / Cape
        drawScope.drawOval(auraColor, Offset(x - 42f, y - 36f), Size(84f, 72f))

        // Giant dark armor body
        drawScope.drawRect(Color(0xFF140026), Offset(x - 22f, y - 24f), Size(44f, 44f))
        drawScope.drawRect(Color(0xFFFFD700), Offset(x - 18f, y - 20f), Size(36f, 6f)) // Golden regal collar

        // Crowned Helm
        drawScope.drawRect(Color(0xFF0B0014), Offset(x - 16f, y - 48f), Size(32f, 26f))
        // Spiked crown
        for (i in -2..2) {
            drawScope.drawRect(Color(0xFFFFB300), Offset(x + (i * 6f) - 2f, y - 56f), Size(4f, 10f))
        }
        // Glowing purple eye slits
        drawScope.drawRect(Color(0xFFE040FB), Offset(x - 10f, y - 38f), Size(8f, 4f))
        drawScope.drawRect(Color(0xFFE040FB), Offset(x + 2f, y - 38f), Size(8f, 4f))

        // Legendary Void Greatsword
        val swordX = if (flip) x - 38f else x + 24f
        drawScope.drawRect(Color(0xFF1A237E), Offset(swordX, y - 54f), Size(10f, 68f))
        drawScope.drawRect(Color(0xFFE040FB), Offset(swordX + 2f, y - 50f), Size(6f, 60f)) // blazing void edge
    }

    // PROPS RENDERING
    fun drawChest(drawScope: DrawScope, chest: Chest, x: Float, y: Float) {
        if (chest.isOpened) {
            drawScope.drawRect(Color(0xFF5D4037), Offset(x - 14f, y - 6f), Size(28f, 16f))
            drawScope.drawRect(Color(0xFF8D6E63), Offset(x - 16f, y - 18f), Size(32f, 12f)) // open lid
            drawScope.drawRect(Color(0xFFFFD700), Offset(x - 8f, y - 2f), Size(16f, 6f)) // gold inside
        } else {
            drawScope.drawRect(Color(0xFF4E342E), Offset(x - 14f, y - 10f), Size(28f, 20f))
            drawScope.drawRect(Color(0xFFFFB300), Offset(x - 14f, y - 3f), Size(28f, 4f)) // Gold band
            drawScope.drawCircle(Color(0xFFFFD700), 4f, Offset(x, y - 1f)) // lock
        }
    }

    fun drawBreakable(drawScope: DrawScope, prop: BreakableProp, x: Float, y: Float) {
        if (prop.isDestroyed) return
        when (prop.type) {
            "barrel" -> {
                drawScope.drawOval(Color(0xFF6D4C41), Offset(x - 12f, y - 14f), Size(24f, 28f))
                drawScope.drawRect(Color(0xFF3E2723), Offset(x - 12f, y - 4f), Size(24f, 4f))
            }
            "pot" -> {
                drawScope.drawCircle(Color(0xFF8D6E63), 12f, Offset(x, y))
                drawScope.drawRect(Color(0xFF5D4037), Offset(x - 6f, y - 14f), Size(12f, 4f))
            }
            "crate" -> {
                drawScope.drawRect(Color(0xFF8D6E63), Offset(x - 12f, y - 12f), Size(24f, 24f))
                drawScope.drawLine(Color(0xFF4E342E), Offset(x - 12f, y - 12f), Offset(x + 12f, y + 12f), strokeWidth = 2f)
            }
        }
    }

    fun drawPortal(drawScope: DrawScope, portal: MapPortal, x: Float, y: Float, animTimer: Float) {
        val radius = 24f + sin(animTimer * 4f) * 4f
        val color1 = if (portal.isLocked) Color(0xFFFF5252) else Color(0xFF00E5FF)
        val color2 = if (portal.isLocked) Color(0xFFB71C1C) else Color(0xFF7C4DFF)

        drawScope.drawCircle(color = color2, radius = radius, center = Offset(x, y))
        drawScope.drawCircle(color = color1, radius = radius * 0.65f, center = Offset(x, y))
        drawScope.drawCircle(color = Color.White, radius = radius * 0.3f, center = Offset(x, y))
    }

    fun drawNpc(drawScope: DrawScope, npc: NpcEntity, x: Float, y: Float) {
        // Shadow
        drawScope.drawOval(Color(0x55000000), Offset(x - 14f, y + 10f), Size(28f, 10f))

        when (npc.spriteId) {
            "valerie" -> {
                // Sage Valerie (white & gold robes, elder staff, halo)
                drawScope.drawRect(Color(0xFFECEFF1), Offset(x - 10f, y - 8f), Size(20f, 20f))
                drawScope.drawRect(Color(0xFFFFD54F), Offset(x - 2f, y - 8f), Size(4f, 20f))
                drawScope.drawRect(Color(0xFFCFD8DC), Offset(x - 8f, y - 22f), Size(16f, 14f)) // hood
                drawScope.drawCircle(Color(0xFFFFEB3B), 2.5f, Offset(x + 14f, y - 18f)) // staff crystal
                drawScope.drawRect(Color(0xFF8D6E63), Offset(x + 12f, y - 16f), Size(4f, 28f)) // staff
            }
            "brom" -> {
                // Blacksmith Brom (leather apron, big beard, holding hammer)
                drawScope.drawRect(Color(0xFF4E342E), Offset(x - 12f, y - 8f), Size(24f, 20f))
                drawScope.drawRect(Color(0xFFFFCC80), Offset(x - 8f, y - 22f), Size(16f, 14f)) // face
                drawScope.drawRect(Color(0xFFD84315), Offset(x - 9f, y - 12f), Size(18f, 10f)) // big ginger beard
                drawScope.drawRect(Color(0xFF37474F), Offset(x + 12f, y - 14f), Size(8f, 12f)) // hammer head
                drawScope.drawRect(Color(0xFF5D4037), Offset(x + 14f, y - 4f), Size(4f, 16f)) // handle
            }
            "zarek" -> {
                // Merchant Zarek (turban, backpack full of items)
                drawScope.drawRect(Color(0xFF00695C), Offset(x - 10f, y - 8f), Size(20f, 20f))
                drawScope.drawRect(Color(0xFF5D4037), Offset(x - 18f, y - 14f), Size(10f, 20f)) // big pack
                drawScope.drawRect(Color(0xFFFFD54F), Offset(x - 8f, y - 24f), Size(16f, 8f)) // gold turban
                drawScope.drawRect(Color(0xFFFFCC80), Offset(x - 7f, y - 16f), Size(14f, 8f)) // face
            }
            else -> {
                // Masked wanderer
                drawScope.drawRect(Color(0xFF263238), Offset(x - 10f, y - 8f), Size(20f, 20f))
                drawScope.drawRect(Color(0xFF1B1B1B), Offset(x - 8f, y - 22f), Size(16f, 14f))
                drawScope.drawCircle(Color(0xFF00E5FF), 2f, Offset(x, y - 15f))
            }
        }

        // Floating Question Mark / Exclamation Mark for Quests
        drawScope.drawCircle(Color(0xFFFFD700), radius = 8f, center = Offset(x, y - 36f))
        drawScope.drawRect(Color.Black, Offset(x - 1.5f, y - 41f), Size(3f, 6f))
        drawScope.drawCircle(Color.Black, radius = 1.5f, center = Offset(x, y - 32f))
    }

    // LIGHTING & AMBIENT ATMOSPHERE
    fun drawAmbientLighting(
        drawScope: DrawScope,
        gameTimeHours: Float,
        playerScreenX: Float,
        playerScreenY: Float,
        isDungeon: Boolean,
        viewWidth: Float,
        viewHeight: Float
    ) {
        // Calculate ambient darkness color
        val darknessAlpha = when {
            isDungeon -> 0.65f // Dungeons are permanently dark
            gameTimeHours in 6f..18f -> {
                // Daytime: no darkness, or gentle sunset at 17-18
                if (gameTimeHours > 17f) (gameTimeHours - 17f) * 0.4f else 0f
            }
            gameTimeHours in 18f..21f -> {
                // Dusk transition
                0.4f + ((gameTimeHours - 18f) / 3f) * 0.35f
            }
            gameTimeHours in 5f..6f -> {
                // Dawn transition
                0.75f - ((gameTimeHours - 5f)) * 0.75f
            }
            else -> {
                // Deep Night: 0.75 darkness
                0.75f
            }
        }

        if (darknessAlpha > 0.05f) {
            // Draw radial light around player
            val lightRadius = if (isDungeon) 170f else 220f
            val brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0x33000000),
                    Color(0xCC050814)
                ),
                center = Offset(playerScreenX, playerScreenY),
                radius = lightRadius
            )
            drawScope.drawRect(brush = brush, size = Size(viewWidth, viewHeight))
        }
    }
}
