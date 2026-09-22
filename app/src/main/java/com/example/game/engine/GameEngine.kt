package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    val audioEngine: AudioEngine,
    val vibrationManager: VibrationManager
) {
    var player: Player = Player()
    var currentRegionId: RegionId = RegionId.OAKWOOD_VILLAGE
    var currentMap: RegionMap = WorldFactory.createRegion(currentRegionId)
    val enemies: MutableList<Enemy> = mutableListOf()
    val projectiles: MutableList<Projectile> = mutableListOf()
    val particleSystem: ParticleSystem = ParticleSystem()
    val quests: MutableList<Quest> = QuestCatalog.createDefaultQuests().toMutableList()

    val openedChests: MutableSet<String> = mutableSetOf()
    val defeatedBosses: MutableSet<String> = mutableSetOf()
    val unlockedWaypoints: MutableSet<String> = mutableSetOf("OAKWOOD_VILLAGE")

    // World ambient time (0.0 to 24.0 hours)
    var gameTimeHours: Float = 9.0f // starts at 9:00 AM morning
    val timeSpeed: Float = 0.2f // 1 real sec = 0.2 game hours

    // Screen Shake
    var screenShakeAmount: Float = 0f
    var screenShakeOffsetX: Float = 0f
    var screenShakeOffsetY: Float = 0f

    // Camera
    var cameraX: Float = 400f
    var cameraY: Float = 400f

    // Active interaction
    var nearbyInteractableText: String? = null
    var activeDialogueNode: DialogueNode? = null
    var isShopOpen: Boolean = false
    var isCraftingOpen: Boolean = false

    // Active Boss reference
    var activeBoss: Enemy? = null

    // Game stats
    var enemiesKilledCount: Int = 0
    var bossesDefeatedCount: Int = 0

    init {
        loadRegion(currentRegionId, 7 * 64f, 10 * 64f)
    }

    fun initNewGame(selectedClass: PlayerClass) {
        player = Player(playerClass = selectedClass)
        player.stats.currentHp = player.playerClass.baseHp
        player.stats.maxHp = player.playerClass.baseHp
        player.stats.currentMana = player.playerClass.baseMana
        player.stats.maxMana = player.playerClass.baseMana
        player.stats.currentStamina = player.playerClass.baseStamina
        player.stats.maxStamina = player.playerClass.baseStamina

        // Starter gear
        when (selectedClass) {
            PlayerClass.KNIGHT -> {
                player.equippedWeapon = ItemCatalog.RUSTY_SWORD
                player.equippedShield = ItemCatalog.WOODEN_BUCKLER
                player.equippedArmor = ItemCatalog.CHAINMAIL_VEST
                player.equippedHelmet = ItemCatalog.IRON_HELM
                player.equippedBoots = ItemCatalog.LEATHER_BOOTS
            }
            PlayerClass.MAGE -> {
                player.equippedWeapon = ItemCatalog.ARCANE_STAFF
                player.equippedArmor = ItemCatalog.CLOTH_TUNIC
                player.equippedHelmet = ItemCatalog.CROWN_OF_SOULS
                player.equippedBoots = ItemCatalog.LEATHER_BOOTS
            }
            PlayerClass.ARCHER -> {
                player.equippedWeapon = ItemCatalog.ELVEN_LONGBOW
                player.equippedArmor = ItemCatalog.CLOTH_TUNIC
                player.equippedBoots = ItemCatalog.WINGED_GREAVES
            }
        }

        player.inventory.add(ItemCatalog.HEALTH_POTION.copy(quantity = 5))
        player.inventory.add(ItemCatalog.MANA_POTION.copy(quantity = 3))
        player.inventory.add(ItemCatalog.STAMINA_ELIXIR.copy(quantity = 2))

        loadRegion(RegionId.OAKWOOD_VILLAGE, 7 * 64f, 10 * 64f)
        audioEngine.startBgm("village")
    }

    fun loadRegion(regionId: RegionId, spawnX: Float, spawnY: Float) {
        currentRegionId = regionId
        currentMap = WorldFactory.createRegion(regionId)
        player.x = spawnX
        player.y = spawnY
        cameraX = spawnX
        cameraY = spawnY

        // Re-apply chest opened states
        currentMap.chests.forEach { c ->
            if (openedChests.contains(c.id)) c.isOpened = true
        }

        // Populate enemies, filtering out defeated bosses
        enemies.clear()
        projectiles.clear()
        for (e in currentMap.defaultEnemies) {
            if (e.isBoss && defeatedBosses.contains(e.id)) continue
            enemies.add(e.copy(currentHp = e.type.maxHp))
        }

        unlockedWaypoints.add(regionId.name)

        // Switch BGM
        val bgmTheme = when (regionId) {
            RegionId.OAKWOOD_VILLAGE -> "village"
            RegionId.WHISPERING_FOREST, RegionId.BLACKTHORN_SWAMP -> "forest"
            RegionId.MISTVEIL_MOUNTAINS, RegionId.FROSTFANG_CAVERNS, RegionId.ANCIENT_RUINS -> "dungeon"
            RegionId.VOID_CITADEL -> "boss"
        }
        audioEngine.startBgm(bgmTheme)
    }

    fun update(dt: Float, viewWidth: Float, viewHeight: Float) {
        // Update Time
        gameTimeHours = (gameTimeHours + dt * timeSpeed) % 24f

        // Screen Shake decay
        if (screenShakeAmount > 0f) {
            screenShakeOffsetX = (Random.nextFloat() - 0.5f) * screenShakeAmount
            screenShakeOffsetY = (Random.nextFloat() - 0.5f) * screenShakeAmount
            screenShakeAmount = (screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        } else {
            screenShakeOffsetX = 0f
            screenShakeOffsetY = 0f
        }

        // Camera follow player smoothly
        val targetCamX = player.x
        val targetCamY = player.y
        cameraX += (targetCamX - cameraX) * (dt * 6f)
        cameraY += (targetCamY - cameraY) * (dt * 6f)

        // Clamp camera to map bounds
        val halfW = viewWidth / 2f
        val halfH = viewHeight / 2f
        cameraX = cameraX.coerceIn(halfW, (currentMap.pixelWidth - halfW).coerceAtLeast(halfW))
        cameraY = cameraY.coerceIn(halfH, (currentMap.pixelHeight - halfH).coerceAtLeast(halfH))

        // Update Player
        updatePlayer(dt)

        // Update Projectiles
        updateProjectiles(dt)

        // Update Enemies
        updateEnemies(dt)

        // Update Particles & Weather
        particleSystem.update(dt)
        particleSystem.spawnWeather(currentMap.id.weatherType, cameraX - halfW, cameraY - halfH, viewWidth, viewHeight)

        // Check active Boss in region
        activeBoss = enemies.firstOrNull { it.isBoss && it.currentHp > 0 }

        // Check contextual interaction nearby
        checkNearbyInteraction()
    }

    private fun updatePlayer(dt: Float) {
        // Cooldowns
        if (player.skill1Cooldown > 0f) player.skill1Cooldown -= dt
        if (player.skill2Cooldown > 0f) player.skill2Cooldown -= dt
        if (player.potionCooldown > 0f) player.potionCooldown -= dt
        if (player.dodgeCooldown > 0f) player.dodgeCooldown -= dt
        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer -= dt
            if (player.invulnerableTimer <= 0f) player.isInvulnerable = false
        }
        if (player.parryTimer > 0f) {
            player.parryTimer -= dt
            if (player.parryTimer <= 0f) player.isParrying = false
        }
        if (player.comboTimer > 0f) {
            player.comboTimer -= dt
            if (player.comboTimer <= 0f) player.comboStep = 0
        }

        // Stamina & Mana regen
        if (player.actionState != PlayerActionState.BLOCKING && player.actionState != PlayerActionState.DODGING) {
            player.stats.currentStamina = (player.stats.currentStamina + 22f * dt).coerceAtMost(player.stats.maxStamina)
        }
        player.stats.currentMana = (player.stats.currentMana + 8f * dt).coerceAtMost(player.stats.maxMana)

        // Action state timer
        if (player.stateTimer > 0f) {
            player.stateTimer -= dt
            if (player.stateTimer <= 0f) {
                if (player.actionState != PlayerActionState.DEAD) {
                    player.actionState = PlayerActionState.IDLE
                }
            }
        }

        // Animation frame cycling
        player.animTimer += dt
        if (player.animTimer >= 0.12f) {
            player.animTimer = 0f
            player.animFrame = (player.animFrame + 1) % 4
        }

        // Apply movement if dodging or running
        if (player.actionState == PlayerActionState.DODGING) {
            val speed = player.getEffectiveSpeed() * 2.2f
            moveWithCollision(player.vx * speed * dt, player.vy * speed * dt)
            particleSystem.spawnDust(player.x, player.y + 14f)
        } else if (player.actionState == PlayerActionState.RUNNING || player.actionState == PlayerActionState.IDLE) {
            val speed = player.getEffectiveSpeed()
            if (player.vx != 0f || player.vy != 0f) {
                player.actionState = PlayerActionState.RUNNING
                moveWithCollision(player.vx * speed * dt, player.vy * speed * dt)
            } else {
                player.actionState = PlayerActionState.IDLE
            }
        }
    }

    private fun moveWithCollision(dx: Float, dy: Float) {
        val newX = player.x + dx
        val newY = player.y + dy

        // Test X
        if (!currentMap.isPositionBlocked(newX, player.y, 16f)) {
            player.x = newX.coerceIn(24f, currentMap.pixelWidth - 24f)
        }
        // Test Y
        if (!currentMap.isPositionBlocked(player.x, newY, 16f)) {
            player.y = newY.coerceIn(24f, currentMap.pixelHeight - 24f)
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dt
            if (p.life <= 0f) {
                iter.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt

            // Check wall collision
            if (currentMap.isPositionBlocked(p.x, p.y, p.radius)) {
                particleSystem.spawnHitSparks(p.x, p.y, 4, p.color)
                iter.remove()
                continue
            }

            // Player projectile hits enemies
            if (p.owner == ProjectileOwner.PLAYER) {
                var hit = false
                for (enemy in enemies) {
                    if (enemy.currentHp <= 0) continue
                    val dist = hypot(p.x - enemy.x, p.y - enemy.y)
                    if (dist < p.radius + 20f) {
                        applyDamageToEnemy(enemy, p.damage, p.isCrit)
                        hit = true
                        break
                    }
                }
                if (hit) iter.remove()
            }
            // Enemy projectile hits player
            else {
                val dist = hypot(p.x - player.x, p.y - player.y)
                if (dist < p.radius + 18f) {
                    applyDamageToPlayer(p.damage)
                    iter.remove()
                }
            }
        }
    }

    private fun updateEnemies(dt: Float) {
        val iter = enemies.iterator()
        while (iter.hasNext()) {
            val e = iter.next()

            // Update Boss phase
            e.updatePhase()

            // State timers
            if (e.attackCooldownTimer > 0f) e.attackCooldownTimer -= dt
            if (e.hurtTimer > 0f) {
                e.hurtTimer -= dt
                // apply knockback
                e.x += e.knockbackVx * dt
                e.y += e.knockbackVy * dt
                e.knockbackVx *= 0.85f
                e.knockbackVy *= 0.85f
            }

            if (e.currentHp <= 0) {
                e.aiState = EnemyAiState.DEAD
                e.stateTimer += dt
                if (e.stateTimer >= 1.2f) {
                    // Enemy removed after death animation
                    handleEnemyDeath(e)
                    iter.remove()
                }
                continue
            }

            // Distance to player
            val dx = player.x - e.x
            val dy = player.y - e.y
            val distToPlayer = hypot(dx, dy)

            // Face player or movement direction
            if (abs(dx) > abs(dy)) {
                e.direction = if (dx > 0) Direction.RIGHT else Direction.LEFT
            } else {
                e.direction = if (dy > 0) Direction.DOWN else Direction.UP
            }

            // AI State Machine
            val aggroRadius = if (e.isBoss) 500f else 280f

            when (e.aiState) {
                EnemyAiState.PATROL -> {
                    if (distToPlayer < aggroRadius) {
                        e.aiState = EnemyAiState.CHASE
                    } else {
                        // Slow wander around spawn
                        e.animTimer += dt
                        if (e.animTimer > 2.5f) {
                            e.animTimer = 0f
                            e.patrolTargetX = e.spawnX + (Random.nextFloat() - 0.5f) * 120f
                            e.patrolTargetY = e.spawnY + (Random.nextFloat() - 0.5f) * 120f
                        }
                    }
                }
                EnemyAiState.CHASE -> {
                    if (distToPlayer > aggroRadius * 1.5f && !e.isBoss) {
                        e.aiState = EnemyAiState.PATROL
                    } else if (distToPlayer <= e.type.attackRange && e.attackCooldownTimer <= 0f) {
                        e.aiState = EnemyAiState.TELEGRAPH_ATTACK
                        e.telegraphTimer = if (e.isBoss) 0.8f else 0.5f
                    } else {
                        // Move toward player
                        val moveSpeed = if (e.isEnraged) e.type.speed * 1.35f else e.type.speed
                        val nx = dx / distToPlayer
                        val ny = dy / distToPlayer
                        val targetX = e.x + nx * moveSpeed * dt
                        val targetY = e.y + ny * moveSpeed * dt
                        if (!currentMap.isPositionBlocked(targetX, e.y, 16f)) e.x = targetX
                        if (!currentMap.isPositionBlocked(e.x, targetY, 16f)) e.y = targetY
                    }
                }
                EnemyAiState.TELEGRAPH_ATTACK -> {
                    e.telegraphTimer -= dt
                    if (e.telegraphTimer <= 0f) {
                        e.aiState = EnemyAiState.ATTACKING
                        e.stateTimer = 0.35f
                        executeEnemyAttack(e)
                    }
                }
                EnemyAiState.ATTACKING -> {
                    e.stateTimer -= dt
                    if (e.stateTimer <= 0f) {
                        e.aiState = EnemyAiState.CHASE
                        e.attackCooldownTimer = e.type.attackCooldown
                    }
                }
                EnemyAiState.HURT -> {
                    if (e.hurtTimer <= 0f) e.aiState = EnemyAiState.CHASE
                }
                EnemyAiState.DEAD -> {}
                else -> {}
            }
        }
    }

    private fun executeEnemyAttack(enemy: Enemy) {
        val dx = player.x - enemy.x
        val dy = player.y - enemy.y
        val dist = hypot(dx, dy)

        // Specific Boss attacks
        if (enemy.isBoss) {
            when (enemy.type) {
                EnemyType.ALPHA_BEHEMOTH -> {
                    screenShakeAmount = 14f
                    vibrationManager.vibrateBossSlam()
                    audioEngine.playHeavyHit()
                    particleSystem.spawnDust(enemy.x, enemy.y)
                    // Shockwave AOE
                    if (dist < 110f) {
                        applyDamageToPlayer(enemy.type.attack * 1.2f)
                    }
                }
                EnemyType.VOIDWRAITH_MORVATH -> {
                    audioEngine.playMagicSpell()
                    // Fires 3 void orbs in spread
                    val baseAngle = atan2(dy, dx)
                    for (offset in listOf(-0.35f, 0f, 0.35f)) {
                        val angle = baseAngle + offset
                        projectiles.add(
                            Projectile(
                                id = "orb_${System.currentTimeMillis()}_$offset",
                                owner = ProjectileOwner.ENEMY,
                                x = enemy.x,
                                y = enemy.y,
                                vx = cos(angle) * 220f,
                                vy = sin(angle) * 220f,
                                damage = enemy.type.attack,
                                color = Color(0xFFBA68C8),
                                effectType = "void"
                            )
                        )
                    }
                }
                EnemyType.LORD_MALAKOR -> {
                    screenShakeAmount = 18f
                    vibrationManager.vibrateBossSlam()
                    audioEngine.playHeavyHit()
                    particleSystem.spawnMagicBurst(enemy.x, enemy.y, Color(0xFF7B1FA2))

                    // Melee cleave
                    if (dist < enemy.type.attackRange + 25f) {
                        applyDamageToPlayer(enemy.type.attack)
                    }
                    // Phase 2 and 3 spawn extra dark wave projectiles
                    if (enemy.currentPhase >= 2) {
                        val baseAngle = atan2(dy, dx)
                        for (i in -2..2) {
                            val a = baseAngle + i * 0.25f
                            projectiles.add(
                                Projectile(
                                    id = "malakor_proj_${System.currentTimeMillis()}_$i",
                                    owner = ProjectileOwner.ENEMY,
                                    x = enemy.x,
                                    y = enemy.y,
                                    vx = cos(a) * 260f,
                                    vy = sin(a) * 260f,
                                    damage = enemy.type.attack * 0.8f,
                                    color = Color(0xFFE91E63),
                                    effectType = "void"
                                )
                            )
                        }
                    }
                }
                else -> {}
            }
        } else if (enemy.type == EnemyType.VOID_CULTIST) {
            audioEngine.playMagicSpell()
            val angle = atan2(dy, dx)
            projectiles.add(
                Projectile(
                    id = "cultist_orb_${System.currentTimeMillis()}",
                    owner = ProjectileOwner.ENEMY,
                    x = enemy.x,
                    y = enemy.y,
                    vx = cos(angle) * 200f,
                    vy = sin(angle) * 200f,
                    damage = enemy.type.attack,
                    color = Color(0xFF9C27B0)
                )
            )
        } else {
            // Standard melee hit
            if (dist < enemy.type.attackRange + 15f) {
                applyDamageToPlayer(enemy.type.attack)
            }
        }
    }

    private fun handleEnemyDeath(enemy: Enemy) {
        enemiesKilledCount++
        particleSystem.spawnBloodSplatter(enemy.x, enemy.y, 14)
        particleSystem.spawnHitSparks(enemy.x, enemy.y, 8, Color.Yellow)

        // Rewards
        val goldEarned = Random.nextInt(enemy.type.goldRewardMin, enemy.type.goldRewardMax + 1)
        player.stats.gold += goldEarned
        particleSystem.spawnStatusText(enemy.x, enemy.y - 30f, "+$goldEarned ذهب", Color(0xFFFFD700))
        audioEngine.playCoin()

        val leveledUp = player.stats.addXp(enemy.type.xpReward)
        particleSystem.spawnStatusText(enemy.x, enemy.y - 50f, "+${enemy.type.xpReward} XP", Color(0xFF64B5F6))
        if (leveledUp) {
            audioEngine.playLevelUp()
            particleSystem.spawnStatusText(player.x, player.y - 45f, "!LEVEL UP", Color(0xFF81C784))
            vibrationManager.vibrateHeavyImpact()
        }

        // Quest tracking
        updateQuestProgress("kill_${enemy.type.name}")

        // Boss specific rewards and flags
        if (enemy.isBoss) {
            bossesDefeatedCount++
            defeatedBosses.add(enemy.id)
            screenShakeAmount = 20f
            vibrationManager.vibrateBossSlam()

            when (enemy.type) {
                EnemyType.ALPHA_BEHEMOTH -> {
                    player.inventory.add(ItemCatalog.DRAGONSCALE_ARMOR)
                    player.inventory.add(ItemCatalog.VOID_ESSENCE.copy(quantity = 3))
                }
                EnemyType.VOIDWRAITH_MORVATH -> {
                    player.inventory.add(ItemCatalog.ANCIENT_TABLET)
                    player.inventory.add(ItemCatalog.CROWN_OF_SOULS)
                }
                EnemyType.LORD_MALAKOR -> {
                    player.inventory.add(ItemCatalog.VOIDBRINGER_GREATSWORD)
                    player.inventory.add(ItemCatalog.AEGIS_OF_LIGHT)
                    particleSystem.spawnStatusText(player.x, player.y - 70f, "تم إنقاذ المملكة وتطهير الفراغ!", Color(0xFFFFD700))
                }
                else -> {}
            }
        }
    }

    // Player Combat Input Handlers
    fun onMoveInput(vx: Float, vy: Float) {
        player.vx = vx
        player.vy = vy

        if (vx != 0f || vy != 0f) {
            if (abs(vx) > abs(vy)) {
                player.direction = if (vx > 0) Direction.RIGHT else Direction.LEFT
            } else {
                player.direction = if (vy > 0) Direction.DOWN else Direction.UP
            }
        }
    }

    fun onNormalAttack() {
        if (player.actionState == PlayerActionState.DEAD || player.actionState == PlayerActionState.DODGING) return
        if (player.stats.currentStamina < 10f) return

        player.stats.currentStamina -= 10f

        // Combo chain calculation
        player.comboStep = (player.comboStep % 3) + 1
        player.comboTimer = 0.9f
        player.actionState = when (player.comboStep) {
            1 -> PlayerActionState.ATTACKING_COMBO_1
            2 -> PlayerActionState.ATTACKING_COMBO_2
            3 -> PlayerActionState.ATTACKING_COMBO_3
            else -> PlayerActionState.ATTACKING_COMBO_1
        }
        player.stateTimer = 0.28f

        val damageMult = when (player.comboStep) {
            1 -> 1.0f
            2 -> 1.25f
            3 -> 1.6f
            else -> 1.0f
        }

        audioEngine.playSlash()
        val dirAngle = getDirectionAngle(player.direction)
        particleSystem.spawnSlashArc(player.x, player.y, dirAngle)

        // If Mage or Archer, spawn projectile!
        if (player.playerClass == PlayerClass.ARCHER) {
            val speed = 500f
            projectiles.add(
                Projectile(
                    id = "arrow_${System.currentTimeMillis()}",
                    owner = ProjectileOwner.PLAYER,
                    x = player.x,
                    y = player.y,
                    vx = cos(dirAngle) * speed,
                    vy = sin(dirAngle) * speed,
                    damage = player.getTotalAtk() * damageMult,
                    isCrit = Random.nextFloat() < player.stats.calculateCritChance(0f),
                    color = Color(0xFFFFEB3B),
                    effectType = "arrow"
                )
            )
        } else if (player.playerClass == PlayerClass.MAGE) {
            val speed = 380f
            projectiles.add(
                Projectile(
                    id = "orb_${System.currentTimeMillis()}",
                    owner = ProjectileOwner.PLAYER,
                    x = player.x,
                    y = player.y,
                    vx = cos(dirAngle) * speed,
                    vy = sin(dirAngle) * speed,
                    damage = player.getTotalAtk() * damageMult,
                    isCrit = Random.nextFloat() < player.stats.calculateCritChance(0f),
                    color = Color(0xFF00E5FF),
                    effectType = "magic"
                )
            )
        } else {
            // Melee Hitbox check
            checkMeleeHit(attackRange = 65f, damageMultiplier = damageMult)
        }
    }

    fun onHeavyAttack() {
        if (player.actionState == PlayerActionState.DEAD || player.actionState == PlayerActionState.DODGING) return
        if (player.stats.currentStamina < 25f) return

        player.stats.currentStamina -= 25f
        player.actionState = PlayerActionState.HEAVY_ATTACK
        player.stateTimer = 0.45f

        audioEngine.playHeavyHit()
        screenShakeAmount = 8f
        vibrationManager.vibrateHeavyImpact()

        val dirAngle = getDirectionAngle(player.direction)
        particleSystem.spawnSlashArc(player.x, player.y, dirAngle, Color(0xFFFF5722))
        checkMeleeHit(attackRange = 85f, damageMultiplier = 2.4f, isHeavy = true)
    }

    fun onDodge() {
        if (player.dodgeCooldown > 0f || player.actionState == PlayerActionState.DEAD) return
        if (player.stats.currentStamina < 20f) return

        player.stats.currentStamina -= 20f
        player.dodgeCooldown = 0.6f
        player.actionState = PlayerActionState.DODGING
        player.stateTimer = 0.28f
        player.isInvulnerable = true
        player.invulnerableTimer = 0.32f

        audioEngine.playDash()
        particleSystem.spawnDust(player.x, player.y)

        // Set dodge velocity
        val angle = getDirectionAngle(player.direction)
        player.vx = cos(angle)
        player.vy = sin(angle)
    }

    fun onBlock(isPressed: Boolean) {
        if (player.actionState == PlayerActionState.DEAD) return
        if (isPressed) {
            if (player.actionState != PlayerActionState.BLOCKING) {
                player.actionState = PlayerActionState.BLOCKING
                player.isParrying = true
                player.parryTimer = 0.22f // 220ms parry window
                audioEngine.playBlock()
            }
        } else {
            if (player.actionState == PlayerActionState.BLOCKING) {
                player.actionState = PlayerActionState.IDLE
                player.isParrying = false
            }
        }
    }

    fun onSkill1() {
        if (player.skill1Cooldown > 0f || player.actionState == PlayerActionState.DEAD) return
        val manaCost = 20f
        if (player.stats.currentMana < manaCost) return

        player.stats.currentMana -= manaCost
        player.skill1Cooldown = 4.0f
        player.actionState = PlayerActionState.CASTING_SKILL_1
        player.stateTimer = 0.4f

        when (player.playerClass) {
            PlayerClass.KNIGHT -> {
                // Whirlwind Slash: 360 damage around player
                audioEngine.playSlash()
                screenShakeAmount = 10f
                for (i in 0 until 12) {
                    val a = (i / 12f) * 2f * Math.PI.toFloat()
                    particleSystem.spawnSlashArc(player.x, player.y, a, Color(0xFF64B5F6))
                }
                for (enemy in enemies) {
                    if (enemy.currentHp <= 0) continue
                    if (hypot(enemy.x - player.x, enemy.y - player.y) < 110f) {
                        applyDamageToEnemy(enemy, player.getTotalAtk() * 2.0f, true)
                    }
                }
            }
            PlayerClass.MAGE -> {
                // Arcane Burst: 3 tracking magic orbs
                audioEngine.playMagicSpell()
                particleSystem.spawnMagicBurst(player.x, player.y, Color(0xFF00E5FF))
                val dirAngle = getDirectionAngle(player.direction)
                for (offset in listOf(-0.35f, 0f, 0.35f)) {
                    val a = dirAngle + offset
                    projectiles.add(
                        Projectile(
                            id = "mage_orb_${System.currentTimeMillis()}_$offset",
                            owner = ProjectileOwner.PLAYER,
                            x = player.x,
                            y = player.y,
                            vx = cos(a) * 350f,
                            vy = sin(a) * 350f,
                            damage = player.getTotalAtk() * 1.8f,
                            color = Color(0xFF00E5FF),
                            effectType = "magic"
                        )
                    )
                }
            }
            PlayerClass.ARCHER -> {
                // Triple Piercing Arrow
                audioEngine.playSlash()
                val dirAngle = getDirectionAngle(player.direction)
                for (offset in listOf(-0.2f, 0f, 0.2f)) {
                    val a = dirAngle + offset
                    projectiles.add(
                        Projectile(
                            id = "archer_triple_${System.currentTimeMillis()}_$offset",
                            owner = ProjectileOwner.PLAYER,
                            x = player.x,
                            y = player.y,
                            vx = cos(a) * 550f,
                            vy = sin(a) * 550f,
                            damage = player.getTotalAtk() * 1.6f,
                            color = Color(0xFFFFD54F),
                            effectType = "arrow"
                        )
                    )
                }
            }
        }
    }

    fun onSkill2() {
        if (player.skill2Cooldown > 0f || player.actionState == PlayerActionState.DEAD) return
        val manaCost = 35f
        if (player.stats.currentMana < manaCost) return

        player.stats.currentMana -= manaCost
        player.skill2Cooldown = 7.0f
        player.actionState = PlayerActionState.CASTING_SKILL_2
        player.stateTimer = 0.5f

        when (player.playerClass) {
            PlayerClass.KNIGHT -> {
                // Shield Bash / Leap Stun
                audioEngine.playHeavyHit()
                screenShakeAmount = 14f
                vibrationManager.vibrateHeavyImpact()
                val angle = getDirectionAngle(player.direction)
                moveWithCollision(cos(angle) * 70f, sin(angle) * 70f)
                checkMeleeHit(attackRange = 90f, damageMultiplier = 2.8f, isHeavy = true)
            }
            PlayerClass.MAGE -> {
                // Meteor Flame Explosion
                audioEngine.playHeavyHit()
                screenShakeAmount = 16f
                val dirAngle = getDirectionAngle(player.direction)
                val targetX = player.x + cos(dirAngle) * 120f
                val targetY = player.y + sin(dirAngle) * 120f
                particleSystem.spawnMagicBurst(targetX, targetY, Color(0xFFFF3D00))
                for (enemy in enemies) {
                    if (enemy.currentHp <= 0) continue
                    if (hypot(enemy.x - targetX, enemy.y - targetY) < 130f) {
                        applyDamageToEnemy(enemy, player.getTotalAtk() * 3.2f, true)
                    }
                }
            }
            PlayerClass.ARCHER -> {
                // Rain of Arrows
                audioEngine.playSlash()
                screenShakeAmount = 12f
                val dirAngle = getDirectionAngle(player.direction)
                val targetX = player.x + cos(dirAngle) * 140f
                val targetY = player.y + sin(dirAngle) * 140f
                for (i in 0 until 10) {
                    val ox = (Random.nextFloat() - 0.5f) * 120f
                    val oy = (Random.nextFloat() - 0.5f) * 120f
                    particleSystem.spawnSlashArc(targetX + ox, targetY + oy, 1.57f, Color(0xFFFFCA28))
                }
                for (enemy in enemies) {
                    if (enemy.currentHp <= 0) continue
                    if (hypot(enemy.x - targetX, enemy.y - targetY) < 110f) {
                        applyDamageToEnemy(enemy, player.getTotalAtk() * 2.6f, true)
                    }
                }
            }
        }
    }

    fun onQuickPotion() {
        if (player.potionCooldown > 0f) return
        val potion = player.inventory.firstOrNull { it.type == ItemType.POTION && it.quantity > 0 } ?: return

        player.potionCooldown = 1.2f
        audioEngine.playPotion()
        particleSystem.spawnMagicBurst(player.x, player.y, Color(0xFF66BB6A))

        when (potion.id) {
            ItemCatalog.HEALTH_POTION.id -> {
                player.stats.currentHp = (player.stats.currentHp + 60f).coerceAtMost(player.stats.maxHp)
                particleSystem.spawnStatusText(player.x, player.y - 30f, "+60 HP", Color(0xFF66BB6A))
            }
            ItemCatalog.MANA_POTION.id -> {
                player.stats.currentMana = (player.stats.currentMana + 50f).coerceAtMost(player.stats.maxMana)
                particleSystem.spawnStatusText(player.x, player.y - 30f, "+50 MP", Color(0xFF42A5F5))
            }
            ItemCatalog.STAMINA_ELIXIR.id -> {
                player.stats.currentStamina = (player.stats.currentStamina + 80f).coerceAtMost(player.stats.maxStamina)
                particleSystem.spawnStatusText(player.x, player.y - 30f, "+80 Stamina", Color(0xFFFFCA28))
            }
            ItemCatalog.GREATER_HEALING.id -> {
                player.stats.currentHp = (player.stats.currentHp + 150f).coerceAtMost(player.stats.maxHp)
                particleSystem.spawnStatusText(player.x, player.y - 30f, "+150 HP", Color(0xFF66BB6A))
            }
        }

        potion.quantity--
        if (potion.quantity <= 0) {
            player.inventory.remove(potion)
        }
    }

    fun onInteract() {
        // 1. Check Portals
        for (portal in currentMap.portals) {
            if (hypot(player.x - portal.x, player.y - portal.y) < 60f) {
                if (portal.isLocked) {
                    val key = player.inventory.firstOrNull { it.id == portal.requiredKeyId }
                    if (key != null) {
                        portal.isLocked = false
                        particleSystem.spawnStatusText(player.x, player.y - 30f, "تم فتح البوابة بالمفتاح!", Color(0xFFFFD700))
                        audioEngine.playChest()
                    } else {
                        particleSystem.spawnStatusText(player.x, player.y - 30f, "البوابة مقفلة! تحتاج إلى مفتاح السراديب", Color(0xFFFF5252))
                        return
                    }
                }
                loadRegion(portal.targetRegion, portal.targetSpawnX, portal.targetSpawnY)
                return
            }
        }

        // 2. Check Chests
        for (chest in currentMap.chests) {
            if (!chest.isOpened && hypot(player.x - chest.x, player.y - chest.y) < 55f) {
                chest.isOpened = true
                openedChests.add(chest.id)
                audioEngine.playChest()
                particleSystem.spawnHitSparks(chest.x, chest.y, 12, Color(0xFFFFD700))

                for (item in chest.contents) {
                    val existing = player.inventory.firstOrNull { it.id == item.id }
                    if (existing != null) {
                        existing.quantity += item.quantity
                    } else {
                        player.inventory.add(item.copy())
                    }
                    particleSystem.spawnStatusText(player.x, player.y - 40f, "وجدت: ${item.name}", Color(item.rarity.colorHex))
                    updateQuestProgress("obtain_${item.id}")
                }
                return
            }
        }

        // 3. Check NPCs
        for (npc in currentMap.npcs) {
            if (hypot(player.x - npc.x, player.y - npc.y) < 70f) {
                activeDialogueNode = DialogueDatabase.getNode(npc.id, "")
                audioEngine.playUiClick()
                updateQuestProgress("talk_${npc.id.replace("npc_", "")}")
                return
            }
        }

        // 4. Check Breakables
        for (prop in currentMap.breakables) {
            if (!prop.isDestroyed && hypot(player.x - prop.x, player.y - prop.y) < 55f) {
                prop.isDestroyed = true
                audioEngine.playHeavyHit()
                particleSystem.spawnDust(prop.x, prop.y)
                for (item in prop.drops) {
                    val existing = player.inventory.firstOrNull { it.id == item.id }
                    if (existing != null) existing.quantity += item.quantity
                    else player.inventory.add(item.copy())
                    particleSystem.spawnStatusText(player.x, player.y - 30f, "+${item.name}", Color(item.rarity.colorHex))
                }
                return
            }
        }
    }

    private fun checkNearbyInteraction() {
        // Portals
        for (portal in currentMap.portals) {
            if (hypot(player.x - portal.x, player.y - portal.y) < 60f) {
                nearbyInteractableText = portal.label
                return
            }
        }
        // Chests
        for (chest in currentMap.chests) {
            if (!chest.isOpened && hypot(player.x - chest.x, player.y - chest.y) < 55f) {
                nearbyInteractableText = "فتح الصندوق"
                return
            }
        }
        // NPCs
        for (npc in currentMap.npcs) {
            if (hypot(player.x - npc.x, player.y - npc.y) < 70f) {
                nearbyInteractableText = "تحدث مع ${npc.name}"
                return
            }
        }
        // Breakables
        for (prop in currentMap.breakables) {
            if (!prop.isDestroyed && hypot(player.x - prop.x, player.y - prop.y) < 55f) {
                nearbyInteractableText = "كسر البرميل"
                return
            }
        }
        nearbyInteractableText = null
    }

    private fun checkMeleeHit(attackRange: Float, damageMultiplier: Float, isHeavy: Boolean = false) {
        val angle = getDirectionAngle(player.direction)
        val hitArcCos = cos(Math.toRadians(60.0)).toFloat() // 120 degree frontal arc

        for (enemy in enemies) {
            if (enemy.currentHp <= 0) continue
            val edx = enemy.x - player.x
            val edy = enemy.y - player.y
            val dist = hypot(edx, edy)

            if (dist < attackRange) {
                val eAngle = atan2(edy, edx)
                val dot = cos(eAngle - angle)
                if (dot >= hitArcCos) {
                    val isCrit = Random.nextFloat() < player.stats.calculateCritChance(0f)
                    var rawDmg = player.getTotalAtk() * damageMultiplier
                    if (isCrit) rawDmg *= 1.8f

                    applyDamageToEnemy(enemy, rawDmg, isCrit)

                    // Apply knockback
                    enemy.knockbackVx = cos(angle) * (if (isHeavy) 320f else 160f)
                    enemy.knockbackVy = sin(angle) * (if (isHeavy) 320f else 160f)
                }
            }
        }
    }

    private fun applyDamageToEnemy(enemy: Enemy, rawDamage: Float, isCrit: Boolean) {
        val finalDamage = (rawDamage - enemy.type.defense).coerceAtLeast(1f)
        enemy.currentHp = (enemy.currentHp - finalDamage).coerceAtLeast(0f)
        enemy.aiState = EnemyAiState.HURT
        enemy.hurtTimer = 0.22f

        particleSystem.spawnDamageNumber(enemy.x, enemy.y, finalDamage.toInt(), isCrit)
        particleSystem.spawnHitSparks(enemy.x, enemy.y, if (isCrit) 12 else 6)
        vibrationManager.vibrateHit()
    }

    private fun applyDamageToPlayer(rawDamage: Float) {
        if (player.isInvulnerable || player.actionState == PlayerActionState.DEAD) return

        // 1. Check Parry Window
        if (player.isParrying) {
            audioEngine.playParry()
            particleSystem.spawnStatusText(player.x, player.y - 30f, "!PARRY", Color(0xFFFFD700))
            particleSystem.spawnHitSparks(player.x, player.y, 14, Color.Cyan)
            vibrationManager.vibrateParry()
            screenShakeAmount = 6f
            return
        }

        // 2. Check Shield Block
        if (player.actionState == PlayerActionState.BLOCKING) {
            audioEngine.playBlock()
            val blockedDamage = (rawDamage * 0.2f).coerceAtLeast(1f)
            player.stats.currentHp = (player.stats.currentHp - blockedDamage).coerceAtLeast(0f)
            player.stats.currentStamina = (player.stats.currentStamina - 15f).coerceAtLeast(0f)
            particleSystem.spawnDamageNumber(player.x, player.y, blockedDamage.toInt(), false, true)
            particleSystem.spawnStatusText(player.x, player.y - 45f, "BLOCKED", Color(0xFF90CAF9))
            vibrationManager.vibrateHit()
            return
        }

        // 3. Direct Damage
        val finalDamage = (rawDamage - player.getTotalDef()).coerceAtLeast(2f)
        player.stats.currentHp = (player.stats.currentHp - finalDamage).coerceAtLeast(0f)
        player.actionState = PlayerActionState.HURT
        player.stateTimer = 0.25f

        screenShakeAmount = 12f
        vibrationManager.vibrateHit()
        audioEngine.playHeavyHit()
        particleSystem.spawnDamageNumber(player.x, player.y, finalDamage.toInt(), false, true)
        particleSystem.spawnBloodSplatter(player.x, player.y, 8)

        if (player.stats.currentHp <= 0f) {
            player.actionState = PlayerActionState.DEAD
            particleSystem.spawnStatusText(player.x, player.y - 50f, "!لقد هلكت", Color(0xFFFF1744))
        }
    }

    fun respawnPlayer() {
        player.stats.currentHp = player.stats.maxHp
        player.stats.currentMana = player.stats.maxMana
        player.stats.currentStamina = player.stats.maxStamina
        player.actionState = PlayerActionState.IDLE
        loadRegion(RegionId.OAKWOOD_VILLAGE, 7 * 64f, 10 * 64f)
    }

    fun updateQuestProgress(targetType: String) {
        for (q in quests) {
            if (!q.isCompleted && q.targetType == targetType) {
                q.currentCount++
                if (q.currentCount >= q.requiredCount) {
                    q.isCompleted = true
                    audioEngine.playLevelUp()
                    particleSystem.spawnStatusText(player.x, player.y - 55f, "اكتملت المهمة: ${q.title}", Color(0xFFFFD700))
                }
            }
        }
    }

    fun claimQuestReward(quest: Quest) {
        if (!quest.isCompleted || quest.isClaimed) return
        quest.isClaimed = true
        player.stats.gold += quest.rewardGold
        val leveledUp = player.stats.addXp(quest.rewardXp)
        if (leveledUp) audioEngine.playLevelUp()

        quest.rewardItem?.let { item ->
            val existing = player.inventory.firstOrNull { it.id == item.id }
            if (existing != null) existing.quantity += item.quantity
            else player.inventory.add(item.copy())
        }
        audioEngine.playCoin()
    }

    private fun getDirectionAngle(dir: Direction): Float {
        return when (dir) {
            Direction.RIGHT -> 0f
            Direction.DOWN -> 1.5708f // PI / 2
            Direction.LEFT -> 3.14159f // PI
            Direction.UP -> 4.71239f // 3 * PI / 2
        }
    }
}
