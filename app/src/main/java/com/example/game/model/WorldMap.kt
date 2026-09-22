package com.example.game.model

enum class RegionId(
    val displayName: String,
    val description: String,
    val isDungeon: Boolean = false,
    val weatherType: WeatherType = WeatherType.CLEAR,
    val dangerLevel: String = "عادي"
) {
    OAKWOOD_VILLAGE("قرية خشب البلوط (Oakwood Village)", "ملاذ آمن يضم الحكيمة فاليري وورشة الحداد بروم ومتجر التاجر زاريك.", false, WeatherType.CLEAR, "آمن 1"),
    WHISPERING_FOREST("غابة الهمسات (Whispering Forest)", "غابة كثيفة الأشجار تحوي ذئاب الظلال وقوافل الغوبلن.", false, WeatherType.RAIN, "منخفض 2"),
    BLACKTHORN_SWAMP("مستنقعات الشوك الأسود (Blackthorn Swamps)", "أراضٍ رطبة ضبابية موطن العناكب السامة والوحش الهائج بلايتكلو.", false, WeatherType.FOG, "متوسط 4"),
    MISTVEIL_MOUNTAINS("جبال الحجاب الضبابي (Mistveil Mountains)", "قمم جليدية شاهقة وأطلال حجرية صخرية خطيرة.", false, WeatherType.SNOW, "مرتفع 6"),
    FROSTFANG_CAVERNS("كهوف الناب الجليدي (Frostfang Caverns)", "مغارات ثلجية جليدية يخفي حراسها مفتاح السراديب.", true, WeatherType.SNOW, "خطر 7"),
    ANCIENT_RUINS("أطلال الصمت القديمة (Ancient Ruins)", "بقايا حضارة إيثلجارد القديمة وحراس سحر الفراغ الغامض.", true, WeatherType.FOG, "شديد الخطورة 8"),
    VOID_CITADEL("قلعة الفراغ المظلمة (Void Citadel)", "معقل سيد الظلال والملك الملعون مالاكور، مركز الكسوف الأبدي.", true, WeatherType.STORM, "قاتل (زعيم) 10")
}

enum class WeatherType(val displayName: String) {
    CLEAR("صافٍ ☀️"),
    RAIN("ممطر 🌧️"),
    FOG("ضباب كثيف 🌫️"),
    SNOW("عاصفة ثلجية ❄️"),
    STORM("برق ورعد ⚡")
}

enum class TileType(val isSolid: Boolean, val colorHex: Long) {
    GRASS(false, 0xFF355E3B),
    GRASS_DARK(false, 0xFF2A4B2F),
    COBBLESTONE(false, 0xFF707070),
    WOOD_FLOOR(false, 0xFF8B5A2B),
    STONE_WALL(true, 0xFF424242),
    WATER(true, 0xFF1E3F66),
    SWAMP_WATER(true, 0xFF2E4035),
    SNOW(false, 0xFFE0E8F0),
    ICE(false, 0xFFB0D0E8),
    VOID_STONE(false, 0xFF221834),
    VOID_WALL(true, 0xFF140D20),
    RUIN_PILLAR(true, 0xFF585858),
    TREE_TRUNK(true, 0xFF4E3629)
}

data class Chest(
    val id: String,
    val x: Float,
    val y: Float,
    var isOpened: Boolean = false,
    val contents: List<Item>
)

data class BreakableProp(
    val id: String,
    val x: Float,
    val y: Float,
    val type: String, // "barrel", "pot", "crate"
    var isDestroyed: Boolean = false,
    val drops: List<Item>
)

data class MapPortal(
    val id: String,
    val x: Float,
    val y: Float,
    val targetRegion: RegionId,
    val targetSpawnX: Float,
    val targetSpawnY: Float,
    val label: String,
    var isLocked: Boolean = false,
    val requiredKeyId: String? = null
)

data class NpcEntity(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val spriteId: String
)

data class RegionMap(
    val id: RegionId,
    val widthTiles: Int = 30,
    val heightTiles: Int = 24,
    val tileSize: Float = 64f,
    val tiles: Array<Array<TileType>>,
    val npcs: MutableList<NpcEntity> = mutableListOf(),
    val chests: MutableList<Chest> = mutableListOf(),
    val breakables: MutableList<BreakableProp> = mutableListOf(),
    val portals: MutableList<MapPortal> = mutableListOf(),
    val defaultEnemies: List<Enemy> = emptyList()
) {
    val pixelWidth: Float get() = widthTiles * tileSize
    val pixelHeight: Float get() = heightTiles * tileSize
    val width: Int get() = widthTiles
    val height: Int get() = heightTiles

    fun getTile(col: Int, row: Int): TileType {
        if (col < 0 || row < 0 || col >= widthTiles || row >= heightTiles) return TileType.STONE_WALL
        return tiles[row][col]
    }

    fun isTileSolid(tx: Int, ty: Int): Boolean {
        if (tx < 0 || ty < 0 || tx >= widthTiles || ty >= heightTiles) return true
        return tiles[ty][tx].isSolid
    }

    fun isPositionBlocked(worldX: Float, worldY: Float, radius: Float = 16f): Boolean {
        val minTx = ((worldX - radius) / tileSize).toInt().coerceIn(0, widthTiles - 1)
        val maxTx = ((worldX + radius) / tileSize).toInt().coerceIn(0, widthTiles - 1)
        val minTy = ((worldY - radius) / tileSize).toInt().coerceIn(0, heightTiles - 1)
        val maxTy = ((worldY + radius) / tileSize).toInt().coerceIn(0, heightTiles - 1)

        for (ty in minTy..maxTy) {
            for (tx in minTx..maxTx) {
                if (tiles[ty][tx].isSolid) return true
            }
        }
        return false
    }
}
