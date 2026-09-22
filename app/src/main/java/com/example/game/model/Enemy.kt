package com.example.game.model

enum class EnemyCategory {
    NORMAL,
    ELITE,
    MINI_BOSS,
    WORLD_BOSS
}

enum class EnemyType(
    val displayName: String,
    val maxHp: Float,
    val attack: Float,
    val defense: Float,
    val speed: Float,
    val attackRange: Float,
    val attackCooldown: Float,
    val xpReward: Int,
    val goldRewardMin: Int,
    val goldRewardMax: Int,
    val category: EnemyCategory = EnemyCategory.NORMAL
) {
    GOBLIN_SCOUT("كشاف الغوبلن", 45f, 12f, 4f, 110f, 40f, 1.2f, 25, 4, 12),
    SHADOW_WOLF("ذئب الظلال", 60f, 16f, 6f, 160f, 45f, 1.0f, 35, 6, 15),
    SKELETON_WARRIOR("محارب الهياكل العظمية", 80f, 20f, 12f, 95f, 45f, 1.5f, 50, 10, 25),
    VENOM_SPIDER("عنكبوت المستنقع السام", 70f, 18f, 8f, 130f, 40f, 1.1f, 45, 8, 20),
    CRYPT_ZOMBIE("زومبي السراديب القديمة", 110f, 22f, 15f, 70f, 38f, 1.8f, 60, 12, 30),
    OUTLAW_BANDIT("قاطع طريق متمرد", 95f, 24f, 14f, 120f, 42f, 1.3f, 70, 18, 40),
    DARK_KNIGHT("فارس الظلام الملعون", 180f, 34f, 25f, 105f, 50f, 1.6f, 120, 35, 75, EnemyCategory.ELITE),
    VOID_CULTIST("ساحر الفراغ المنبوذ", 120f, 38f, 10f, 90f, 160f, 2.0f, 110, 30, 65, EnemyCategory.ELITE),
    
    // Bosses
    ALPHA_BEHEMOTH("الوحش الهائج: بلايتكلو (Blightclaw)", 500f, 42f, 20f, 100f, 65f, 2.2f, 350, 150, 300, EnemyCategory.MINI_BOSS),
    VOIDWRAITH_MORVATH("حارس السراديب: طيف مورفاث (Morvath)", 850f, 55f, 28f, 115f, 140f, 1.8f, 750, 350, 600, EnemyCategory.MINI_BOSS),
    LORD_MALAKOR("سيد الظلال: الملك الملعون مالاكور (Lord Malakor)", 1600f, 70f, 35f, 130f, 75f, 1.5f, 2000, 1000, 2000, EnemyCategory.WORLD_BOSS)
}

enum class EnemyAiState {
    PATROL,
    ALERT,
    CHASE,
    TELEGRAPH_ATTACK,
    ATTACKING,
    HURT,
    DEAD
}

data class Enemy(
    val id: String,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var currentHp: Float = type.maxHp,
    var aiState: EnemyAiState = EnemyAiState.PATROL,
    var direction: Direction = Direction.DOWN,
    
    var stateTimer: Float = 0f,
    var attackCooldownTimer: Float = 0f,
    var telegraphTimer: Float = 0f,
    var animFrame: Int = 0,
    var animTimer: Float = 0f,
    
    // Patrol logic
    var spawnX: Float = x,
    var spawnY: Float = y,
    var patrolTargetX: Float = x,
    var patrolTargetY: Float = y,
    
    // Boss multi-phase logic
    var currentPhase: Int = 1,
    var isEnraged: Boolean = false,
    
    // Hit reaction
    var hurtTimer: Float = 0f,
    var knockbackVx: Float = 0f,
    var knockbackVy: Float = 0f
) {
    val isBoss: Boolean
        get() = type.category == EnemyCategory.MINI_BOSS || type.category == EnemyCategory.WORLD_BOSS

    fun updatePhase() {
        if (!isBoss) return
        val hpPercent = currentHp / type.maxHp
        if (type == EnemyType.LORD_MALAKOR) {
            when {
                hpPercent <= 0.33f && currentPhase < 3 -> {
                    currentPhase = 3
                    isEnraged = true
                }
                hpPercent <= 0.66f && currentPhase < 2 -> {
                    currentPhase = 2
                }
            }
        } else if (type == EnemyType.ALPHA_BEHEMOTH || type == EnemyType.VOIDWRAITH_MORVATH) {
            if (hpPercent <= 0.5f && currentPhase < 2) {
                currentPhase = 2
                isEnraged = true
            }
        }
    }
}
