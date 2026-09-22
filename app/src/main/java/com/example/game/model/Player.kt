package com.example.game.model

enum class PlayerClass(val displayName: String, val description: String, val baseHp: Float, val baseMana: Float, val baseStamina: Float, val baseAtk: Float, val baseDef: Float) {
    KNIGHT("الفارس (Knight)", "مقاتل مدرع يتميز بالدفاع العالي والضربات القاطعة والسيف والدرع.", 120f, 40f, 100f, 22f, 15f),
    MAGE("الساحر (Mage)", "سيد العناصر السحرية، يعتمد على إطلاق كرات النار والصواعق من مسافة بعيدة.", 75f, 130f, 80f, 30f, 6f),
    ARCHER("الرامي (Archer)", "صياد رشيق وسريع يطلق سهاماً خارقة ولديه مراوغة فائقة وضربات حرجة.", 90f, 60f, 110f, 25f, 9f)
}

enum class PlayerActionState {
    IDLE,
    RUNNING,
    ATTACKING_COMBO_1,
    ATTACKING_COMBO_2,
    ATTACKING_COMBO_3,
    HEAVY_ATTACK,
    DODGING,
    BLOCKING,
    CASTING_SKILL_1,
    CASTING_SKILL_2,
    HURT,
    DEAD
}

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}

data class PlayerStats(
    var level: Int = 1,
    var xp: Int = 0,
    var xpToNext: Int = 100,
    var statPoints: Int = 0,
    var skillPoints: Int = 0,
    
    // Core attributes
    var strength: Int = 10,
    var agility: Int = 10,
    var intelligence: Int = 10,
    var vitality: Int = 10,
    
    // Derived
    var maxHp: Float = 120f,
    var currentHp: Float = 120f,
    var maxMana: Float = 50f,
    var currentMana: Float = 50f,
    var maxStamina: Float = 100f,
    var currentStamina: Float = 100f,
    
    var gold: Int = 150
) {
    fun calculateTotalAtk(baseAtk: Float, weaponAtk: Float): Float {
        return baseAtk + weaponAtk + (strength * 1.5f) + (intelligence * 0.8f)
    }
    
    fun calculateTotalDef(baseDef: Float, armorDef: Float, shieldDef: Float): Float {
        return baseDef + armorDef + shieldDef + (vitality * 1.2f)
    }
    
    fun calculateCritChance(agilityBonus: Float): Float {
        return (0.05f + (agility * 0.01f) + agilityBonus).coerceIn(0.05f, 0.75f)
    }

    fun calculateMaxHp(baseHp: Float): Float {
        return baseHp + (vitality * 10f)
    }
    
    fun addXp(amount: Int): Boolean {
        xp += amount
        var leveledUp = false
        while (xp >= xpToNext) {
            xp -= xpToNext
            level++
            xpToNext = (xpToNext * 1.5f).toInt()
            statPoints += 3
            skillPoints += 1
            maxHp += 15f
            currentHp = maxHp
            maxMana += 10f
            currentMana = maxMana
            maxStamina += 10f
            currentStamina = maxStamina
            leveledUp = true
        }
        return leveledUp
    }
}

data class Player(
    var playerClass: PlayerClass = PlayerClass.KNIGHT,
    var x: Float = 400f,
    var y: Float = 400f,
    var direction: Direction = Direction.RIGHT,
    var actionState: PlayerActionState = PlayerActionState.IDLE,
    var stateTimer: Float = 0f,
    var animFrame: Int = 0,
    var animTimer: Float = 0f,
    
    val stats: PlayerStats = PlayerStats(),
    
    // Cooldown timers
    var skill1Cooldown: Float = 0f,
    var skill2Cooldown: Float = 0f,
    var potionCooldown: Float = 0f,
    var dodgeCooldown: Float = 0f,
    var comboTimer: Float = 0f,
    var comboStep: Int = 0,
    
    // Defensive/Parry timing
    var isParrying: Boolean = false,
    var parryTimer: Float = 0f,
    var isInvulnerable: Boolean = false,
    var invulnerableTimer: Float = 0f,
    
    // Movement velocity
    var vx: Float = 0f,
    var vy: Float = 0f,
    
    // Equipped gear
    var equippedWeapon: Item? = null,
    var equippedShield: Item? = null,
    var equippedArmor: Item? = null,
    var equippedHelmet: Item? = null,
    var equippedBoots: Item? = null,
    var equippedRing: Item? = null,
    
    // Inventory
    val inventory: MutableList<Item> = mutableListOf(),
    val unlockedSkills: MutableSet<String> = mutableSetOf()
) {
    fun getEffectiveSpeed(): Float {
        val baseSpeed = when (playerClass) {
            PlayerClass.KNIGHT -> 180f
            PlayerClass.MAGE -> 195f
            PlayerClass.ARCHER -> 225f
        }
        val bootsSpeed = equippedBoots?.speedBonus ?: 0f
        return baseSpeed + bootsSpeed + (stats.agility * 2f)
    }

    fun getTotalAtk(): Float {
        val weaponAtk = equippedWeapon?.attackBonus ?: 10f
        val ringAtk = equippedRing?.attackBonus ?: 0f
        return stats.calculateTotalAtk(playerClass.baseAtk, weaponAtk + ringAtk)
    }

    fun getTotalDef(): Float {
        val armorDef = equippedArmor?.defenseBonus ?: 5f
        val shieldDef = equippedShield?.defenseBonus ?: 0f
        val helmDef = equippedHelmet?.defenseBonus ?: 0f
        val ringDef = equippedRing?.defenseBonus ?: 0f
        return stats.calculateTotalDef(playerClass.baseDef, armorDef + helmDef + ringDef, shieldDef)
    }

    fun isEquipped(item: Item): Boolean {
        return equippedWeapon?.id == item.id ||
                equippedShield?.id == item.id ||
                equippedArmor?.id == item.id ||
                equippedHelmet?.id == item.id ||
                equippedBoots?.id == item.id ||
                equippedRing?.id == item.id
    }

    fun equipItem(item: Item) {
        when (item.type) {
            ItemType.WEAPON -> equippedWeapon = item
            ItemType.SHIELD -> equippedShield = item
            ItemType.ARMOR -> equippedArmor = item
            ItemType.HELMET -> equippedHelmet = item
            ItemType.BOOTS -> equippedBoots = item
            ItemType.RING -> equippedRing = item
            else -> {}
        }
    }

    fun unequipItem(item: Item) {
        when (item.type) {
            ItemType.WEAPON -> if (equippedWeapon?.id == item.id) equippedWeapon = null
            ItemType.SHIELD -> if (equippedShield?.id == item.id) equippedShield = null
            ItemType.ARMOR -> if (equippedArmor?.id == item.id) equippedArmor = null
            ItemType.HELMET -> if (equippedHelmet?.id == item.id) equippedHelmet = null
            ItemType.BOOTS -> if (equippedBoots?.id == item.id) equippedBoots = null
            ItemType.RING -> if (equippedRing?.id == item.id) equippedRing = null
            else -> {}
        }
    }
}
