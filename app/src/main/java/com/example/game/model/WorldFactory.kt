package com.example.game.model

object WorldFactory {

    fun createRegion(regionId: RegionId): RegionMap {
        return when (regionId) {
            RegionId.OAKWOOD_VILLAGE -> createOakwoodVillage()
            RegionId.WHISPERING_FOREST -> createWhisperingForest()
            RegionId.BLACKTHORN_SWAMP -> createBlackthornSwamp()
            RegionId.MISTVEIL_MOUNTAINS -> createMistveilMountains()
            RegionId.FROSTFANG_CAVERNS -> createFrostfangCaverns()
            RegionId.ANCIENT_RUINS -> createAncientRuins()
            RegionId.VOID_CITADEL -> createVoidCitadel()
        }
    }

    private fun createOakwoodVillage(): RegionMap {
        val w = 28
        val h = 22
        val tiles = Array(h) { Array(w) { TileType.GRASS } }

        // Borders
        for (x in 0 until w) {
            tiles[0][x] = TileType.STONE_WALL
            tiles[h - 1][x] = TileType.STONE_WALL
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.STONE_WALL
            tiles[y][w - 1] = TileType.STONE_WALL
        }

        // Cobblestone Main Street
        for (x in 3 until w - 3) {
            tiles[10][x] = TileType.COBBLESTONE
            tiles[11][x] = TileType.COBBLESTONE
        }
        for (y in 4 until h - 4) {
            tiles[y][12] = TileType.COBBLESTONE
            tiles[y][13] = TileType.COBBLESTONE
        }

        // Village Pond
        for (y in 3..6) {
            for (x in 3..6) {
                tiles[y][x] = TileType.WATER
            }
        }

        // Blacksmith House (top right)
        for (y in 3..7) {
            for (x in 18..23) {
                if (y == 3 || y == 7 || x == 18 || x == 23) {
                    tiles[y][x] = TileType.STONE_WALL
                } else {
                    tiles[y][x] = TileType.WOOD_FLOOR
                }
            }
        }
        tiles[7][20] = TileType.WOOD_FLOOR // Door

        // Elder Shrine (bottom left)
        for (y in 14..18) {
            for (x in 4..9) {
                if (y == 14 || y == 18 || x == 4 || x == 9) {
                    tiles[y][x] = TileType.RUIN_PILLAR
                } else {
                    tiles[y][x] = TileType.COBBLESTONE
                }
            }
        }
        tiles[14][6] = TileType.COBBLESTONE // Entrance

        // Eastern Gate opening
        tiles[10][w - 1] = TileType.COBBLESTONE
        tiles[11][w - 1] = TileType.COBBLESTONE

        // Northern Gate opening
        tiles[0][12] = TileType.COBBLESTONE
        tiles[0][13] = TileType.COBBLESTONE

        val npcs = mutableListOf(
            NpcEntity("npc_valerie", "الحكيمة فاليري", 7 * 64f, 16 * 64f, "valerie"),
            NpcEntity("npc_brom", "الحداد بروم", 20 * 64f, 8 * 64f, "brom"),
            NpcEntity("npc_zarek", "التاجر زاريك", 14 * 64f, 12 * 64f, "zarek"),
            NpcEntity("npc_wanderer", "المسافر المقنع", 9 * 64f, 5 * 64f, "wanderer")
        )

        val chests = mutableListOf(
            Chest("c_village_1", 21 * 64f, 5 * 64f, false, listOf(ItemCatalog.HEALTH_POTION.copy(quantity = 2), ItemCatalog.IRON_ORE.copy(quantity = 2)))
        )

        val breakables = mutableListOf(
            BreakableProp("b_v1", 17 * 64f, 9 * 64f, "barrel", false, listOf(ItemCatalog.HEALTH_POTION)),
            BreakableProp("b_v2", 18 * 64f, 9 * 64f, "pot", false, listOf(ItemCatalog.HEALING_HERB.copy(quantity = 2))),
            BreakableProp("b_v3", 11 * 64f, 15 * 64f, "crate", false, listOf(ItemCatalog.MANA_POTION))
        )

        val portals = mutableListOf(
            MapPortal("p_to_forest", (w - 1) * 64f, 10.5f * 64f, RegionId.WHISPERING_FOREST, 2 * 64f, 10.5f * 64f, "إلى غابة الهمسات"),
            MapPortal("p_to_mountains", 12.5f * 64f, 1 * 64f, RegionId.MISTVEIL_MOUNTAINS, 12.5f * 64f, 19 * 64f, "إلى جبال الحجاب الضبابي")
        )

        return RegionMap(RegionId.OAKWOOD_VILLAGE, w, h, 64f, tiles, npcs, chests, breakables, portals)
    }

    private fun createWhisperingForest(): RegionMap {
        val w = 30
        val h = 24
        val tiles = Array(h) { Array(w) { TileType.GRASS } }

        for (x in 0 until w) {
            tiles[0][x] = TileType.TREE_TRUNK
            tiles[h - 1][x] = TileType.TREE_TRUNK
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.TREE_TRUNK
            tiles[y][w - 1] = TileType.TREE_TRUNK
        }

        // River through forest
        for (y in 4 until h - 4) {
            val rx = (14 + kotlin.math.sin(y * 0.4).toInt()).coerceIn(1, w - 2)
            tiles[y][rx] = TileType.WATER
            tiles[y][rx + 1] = TileType.WATER
        }
        // Wooden bridge across river
        tiles[10][14] = TileType.WOOD_FLOOR
        tiles[10][15] = TileType.WOOD_FLOOR
        tiles[11][14] = TileType.WOOD_FLOOR
        tiles[11][15] = TileType.WOOD_FLOOR

        // Clusters of trees
        for (i in 0 until 25) {
            val tx = (3 + (i * 7) % (w - 6))
            val ty = (3 + (i * 11) % (h - 6))
            if (tx != 14 && tx != 15) {
                tiles[ty][tx] = TileType.TREE_TRUNK
            }
        }

        // Gates
        tiles[10][0] = TileType.GRASS // West back to village
        tiles[11][0] = TileType.GRASS
        tiles[h - 1][14] = TileType.GRASS // South to Swamp
        tiles[h - 1][15] = TileType.GRASS
        tiles[11][w - 1] = TileType.GRASS // East to Ancient Ruins
        tiles[12][w - 1] = TileType.GRASS

        val chests = mutableListOf(
            Chest("c_forest_1", 24 * 64f, 4 * 64f, false, listOf(ItemCatalog.ELVEN_LONGBOW, ItemCatalog.HEALING_HERB.copy(quantity = 3))),
            Chest("c_forest_2", 6 * 64f, 18 * 64f, false, listOf(ItemCatalog.WOLF_PELT.copy(quantity = 4), ItemCatalog.STAMINA_ELIXIR))
        )

        val breakables = mutableListOf(
            BreakableProp("b_f1", 8 * 64f, 8 * 64f, "pot", false, listOf(ItemCatalog.HEALING_HERB)),
            BreakableProp("b_f2", 22 * 64f, 16 * 64f, "crate", false, listOf(ItemCatalog.IRON_ORE.copy(quantity = 2)))
        )

        val portals = mutableListOf(
            MapPortal("p_forest_to_village", 0.5f * 64f, 10.5f * 64f, RegionId.OAKWOOD_VILLAGE, 26 * 64f, 10.5f * 64f, "إلى قرية خشب البلوط"),
            MapPortal("p_forest_to_swamp", 14.5f * 64f, (h - 1) * 64f, RegionId.BLACKTHORN_SWAMP, 14.5f * 64f, 2 * 64f, "إلى مستنقعات الشوك الأسود"),
            MapPortal("p_forest_to_ruins", (w - 1) * 64f, 11.5f * 64f, RegionId.ANCIENT_RUINS, 2 * 64f, 11.5f * 64f, "إلى أطلال الصمت القديمة")
        )

        val enemies = listOf(
            Enemy("wf_1", EnemyType.SHADOW_WOLF, 6 * 64f, 7 * 64f),
            Enemy("wf_2", EnemyType.SHADOW_WOLF, 8 * 64f, 14 * 64f),
            Enemy("wf_3", EnemyType.SHADOW_WOLF, 20 * 64f, 6 * 64f),
            Enemy("wf_4", EnemyType.SHADOW_WOLF, 22 * 64f, 18 * 64f),
            Enemy("gb_1", EnemyType.GOBLIN_SCOUT, 18 * 64f, 12 * 64f),
            Enemy("gb_2", EnemyType.GOBLIN_SCOUT, 24 * 64f, 10 * 64f),
            Enemy("bd_1", EnemyType.OUTLAW_BANDIT, 25 * 64f, 14 * 64f)
        )

        return RegionMap(RegionId.WHISPERING_FOREST, w, h, 64f, tiles, chests = chests, breakables = breakables, portals = portals, defaultEnemies = enemies)
    }

    private fun createBlackthornSwamp(): RegionMap {
        val w = 28
        val h = 24
        val tiles = Array(h) { Array(w) { TileType.GRASS_DARK } }

        for (x in 0 until w) {
            tiles[0][x] = TileType.TREE_TRUNK
            tiles[h - 1][x] = TileType.TREE_TRUNK
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.TREE_TRUNK
            tiles[y][w - 1] = TileType.TREE_TRUNK
        }

        // Swamp Water pools
        for (y in 5..9) {
            for (x in 4..11) tiles[y][x] = TileType.SWAMP_WATER
        }
        for (y in 13..18) {
            for (x in 15..22) tiles[y][x] = TileType.SWAMP_WATER
        }

        // North opening to forest
        tiles[0][14] = TileType.GRASS_DARK
        tiles[0][15] = TileType.GRASS_DARK

        // Boss Arena in the center/south
        val boss = Enemy("boss_behemoth", EnemyType.ALPHA_BEHEMOTH, 14 * 64f, 14 * 64f)

        val chests = mutableListOf(
            Chest("c_swamp_1", 8 * 64f, 4 * 64f, false, listOf(ItemCatalog.VOID_ESSENCE, ItemCatalog.GREATER_HEALING)),
            Chest("c_swamp_boss", 14 * 64f, 20 * 64f, false, listOf(ItemCatalog.TOWER_SHIELD, ItemCatalog.MONSTER_BONE.copy(quantity = 3)))
        )

        val portals = mutableListOf(
            MapPortal("p_swamp_to_forest", 14.5f * 64f, 0.5f * 64f, RegionId.WHISPERING_FOREST, 14.5f * 64f, 21 * 64f, "إلى غابة الهمسات")
        )

        val enemies = listOf(
            Enemy("sp_1", EnemyType.VENOM_SPIDER, 7 * 64f, 11 * 64f),
            Enemy("sp_2", EnemyType.VENOM_SPIDER, 19 * 64f, 8 * 64f),
            Enemy("zm_1", EnemyType.CRYPT_ZOMBIE, 10 * 64f, 17 * 64f),
            Enemy("zm_2", EnemyType.CRYPT_ZOMBIE, 20 * 64f, 16 * 64f),
            boss
        )

        return RegionMap(RegionId.BLACKTHORN_SWAMP, w, h, 64f, tiles, chests = chests, portals = portals, defaultEnemies = enemies)
    }

    private fun createMistveilMountains(): RegionMap {
        val w = 26
        val h = 22
        val tiles = Array(h) { Array(w) { TileType.STONE_WALL } }

        // Carve mountain pathways
        for (y in 2 until h - 2) {
            for (x in 2 until w - 2) {
                tiles[y][x] = TileType.COBBLESTONE
            }
        }
        // Mountain ridges
        for (y in 5..16) {
            tiles[y][8] = TileType.STONE_WALL
            tiles[y][16] = TileType.STONE_WALL
        }
        // Passages
        tiles[8][8] = TileType.COBBLESTONE
        tiles[14][16] = TileType.COBBLESTONE

        // South entrance from village
        tiles[h - 1][12] = TileType.COBBLESTONE
        tiles[h - 1][13] = TileType.COBBLESTONE

        // North cave entrance to Frostfang Caverns
        tiles[1][12] = TileType.ICE
        tiles[1][13] = TileType.ICE

        val chests = mutableListOf(
            Chest("c_mount_1", 4 * 64f, 4 * 64f, false, listOf(ItemCatalog.WINGED_GREAVES, ItemCatalog.IRON_ORE.copy(quantity = 5)))
        )

        val portals = mutableListOf(
            MapPortal("p_mount_to_village", 12.5f * 64f, (h - 1) * 64f, RegionId.OAKWOOD_VILLAGE, 12.5f * 64f, 2 * 64f, "إلى قرية خشب البلوط"),
            MapPortal("p_mount_to_caverns", 12.5f * 64f, 1.5f * 64f, RegionId.FROSTFANG_CAVERNS, 12.5f * 64f, 18 * 64f, "إلى كهوف الناب الجليدي")
        )

        val enemies = listOf(
            Enemy("sk_m1", EnemyType.SKELETON_WARRIOR, 5 * 64f, 10 * 64f),
            Enemy("sk_m2", EnemyType.SKELETON_WARRIOR, 12 * 64f, 11 * 64f),
            Enemy("bd_m1", EnemyType.OUTLAW_BANDIT, 20 * 64f, 6 * 64f),
            Enemy("bd_m2", EnemyType.OUTLAW_BANDIT, 21 * 64f, 15 * 64f)
        )

        return RegionMap(RegionId.MISTVEIL_MOUNTAINS, w, h, 64f, tiles, chests = chests, portals = portals, defaultEnemies = enemies)
    }

    private fun createFrostfangCaverns(): RegionMap {
        val w = 26
        val h = 22
        val tiles = Array(h) { Array(w) { TileType.ICE } }

        for (x in 0 until w) {
            tiles[0][x] = TileType.STONE_WALL
            tiles[h - 1][x] = TileType.STONE_WALL
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.STONE_WALL
            tiles[y][w - 1] = TileType.STONE_WALL
        }

        // Ice columns
        for (y in 4..16 step 3) {
            for (x in 5..20 step 5) {
                tiles[y][x] = TileType.STONE_WALL
            }
        }

        // South exit back to mountains
        tiles[h - 1][12] = TileType.ICE
        tiles[h - 1][13] = TileType.ICE

        val chests = mutableListOf(
            Chest("c_crypt_key", 12.5f * 64f, 4 * 64f, false, listOf(ItemCatalog.CRYPT_KEY, ItemCatalog.GREATER_HEALING.copy(quantity = 2)))
        )

        val portals = mutableListOf(
            MapPortal("p_caverns_to_mount", 12.5f * 64f, (h - 1) * 64f, RegionId.MISTVEIL_MOUNTAINS, 12.5f * 64f, 3 * 64f, "إلى جبال الحجاب الضبابي")
        )

        val enemies = listOf(
            Enemy("fs_1", EnemyType.SKELETON_WARRIOR, 7 * 64f, 8 * 64f),
            Enemy("fs_2", EnemyType.SKELETON_WARRIOR, 18 * 64f, 8 * 64f),
            Enemy("fs_3", EnemyType.DARK_KNIGHT, 12.5f * 64f, 7 * 64f)
        )

        return RegionMap(RegionId.FROSTFANG_CAVERNS, w, h, 64f, tiles, chests = chests, portals = portals, defaultEnemies = enemies)
    }

    private fun createAncientRuins(): RegionMap {
        val w = 28
        val h = 24
        val tiles = Array(h) { Array(w) { TileType.COBBLESTONE } }

        for (x in 0 until w) {
            tiles[0][x] = TileType.STONE_WALL
            tiles[h - 1][x] = TileType.STONE_WALL
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.STONE_WALL
            tiles[y][w - 1] = TileType.STONE_WALL
        }

        // Ruin pillars
        for (y in 4..18 step 4) {
            for (x in 4..22 step 4) {
                tiles[y][x] = TileType.RUIN_PILLAR
            }
        }

        // West entrance from forest
        tiles[11][0] = TileType.COBBLESTONE
        tiles[12][0] = TileType.COBBLESTONE

        // East locked portal to Void Citadel
        tiles[11][w - 1] = TileType.VOID_STONE
        tiles[12][w - 1] = TileType.VOID_STONE

        val miniBoss = Enemy("boss_morvath", EnemyType.VOIDWRAITH_MORVATH, 20 * 64f, 12 * 64f)

        val chests = mutableListOf(
            Chest("c_ruins_tablet", 20 * 64f, 4 * 64f, false, listOf(ItemCatalog.ANCIENT_TABLET, ItemCatalog.CROWN_OF_SOULS))
        )

        val portals = mutableListOf(
            MapPortal("p_ruins_to_forest", 0.5f * 64f, 11.5f * 64f, RegionId.WHISPERING_FOREST, 27 * 64f, 11.5f * 64f, "إلى غابة الهمسات"),
            MapPortal("p_ruins_to_citadel", (w - 1) * 64f, 11.5f * 64f, RegionId.VOID_CITADEL, 2 * 64f, 11.5f * 64f, "إلى قلعة الفراغ (يتطلب مفتاح السراديب)", isLocked = true, requiredKeyId = "q_crypt_key")
        )

        val enemies = listOf(
            Enemy("vc_1", EnemyType.VOID_CULTIST, 8 * 64f, 8 * 64f),
            Enemy("vc_2", EnemyType.VOID_CULTIST, 14 * 64f, 16 * 64f),
            Enemy("dk_1", EnemyType.DARK_KNIGHT, 15 * 64f, 10 * 64f),
            miniBoss
        )

        return RegionMap(RegionId.ANCIENT_RUINS, w, h, 64f, tiles, chests = chests, portals = portals, defaultEnemies = enemies)
    }

    private fun createVoidCitadel(): RegionMap {
        val w = 28
        val h = 24
        val tiles = Array(h) { Array(w) { TileType.VOID_STONE } }

        for (x in 0 until w) {
            tiles[0][x] = TileType.VOID_WALL
            tiles[h - 1][x] = TileType.VOID_WALL
        }
        for (y in 0 until h) {
            tiles[y][0] = TileType.VOID_WALL
            tiles[y][w - 1] = TileType.VOID_WALL
        }

        // Void pillars
        for (y in 5..17 step 4) {
            tiles[y][6] = TileType.VOID_WALL
            tiles[y][w - 7] = TileType.VOID_WALL
        }

        // West entrance from Ruins
        tiles[11][0] = TileType.VOID_STONE
        tiles[12][0] = TileType.VOID_STONE

        // The Final Boss: Lord Malakor in center throne!
        val lordMalakor = Enemy("boss_malakor", EnemyType.LORD_MALAKOR, 18 * 64f, 12 * 64f)

        val chests = mutableListOf(
            Chest("c_void_hoard", 23 * 64f, 5 * 64f, false, listOf(ItemCatalog.AEGIS_OF_LIGHT, ItemCatalog.RING_BERSERKER, ItemCatalog.VOID_ESSENCE.copy(quantity = 5)))
        )

        val portals = mutableListOf(
            MapPortal("p_citadel_to_ruins", 0.5f * 64f, 11.5f * 64f, RegionId.ANCIENT_RUINS, 25 * 64f, 11.5f * 64f, "إلى أطلال الصمت")
        )

        val enemies = listOf(
            Enemy("dk_c1", EnemyType.DARK_KNIGHT, 10 * 64f, 8 * 64f),
            Enemy("dk_c2", EnemyType.DARK_KNIGHT, 10 * 64f, 16 * 64f),
            Enemy("vc_c1", EnemyType.VOID_CULTIST, 14 * 64f, 6 * 64f),
            Enemy("vc_c2", EnemyType.VOID_CULTIST, 14 * 64f, 18 * 64f),
            lordMalakor
        )

        return RegionMap(RegionId.VOID_CITADEL, w, h, 64f, tiles, chests = chests, portals = portals, defaultEnemies = enemies)
    }
}
