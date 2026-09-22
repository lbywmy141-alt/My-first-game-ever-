package com.example.game.data

import android.content.Context
import com.example.game.model.*
import org.json.JSONArray
import org.json.JSONObject

data class SaveSlotSummary(
    val slotIndex: Int,
    val exists: Boolean,
    val playerClassName: String = "",
    val level: Int = 1,
    val gold: Int = 0,
    val currentRegion: String = "",
    val timestamp: Long = 0L
)

class SaveManager(context: Context) {
    private val prefs = context.getSharedPreferences("dark_realm_rpg_saves", Context.MODE_PRIVATE)

    fun getSlotSummary(slotIndex: Int): SaveSlotSummary {
        val jsonStr = prefs.getString("save_slot_$slotIndex", null) ?: return SaveSlotSummary(slotIndex, false)
        return try {
            val json = JSONObject(jsonStr)
            SaveSlotSummary(
                slotIndex = slotIndex,
                exists = true,
                playerClassName = json.optString("playerClass", "KNIGHT"),
                level = json.optInt("level", 1),
                gold = json.optInt("gold", 0),
                currentRegion = json.optString("currentRegion", "OAKWOOD_VILLAGE"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            SaveSlotSummary(slotIndex, false)
        }
    }

    fun saveGame(
        slotIndex: Int,
        player: Player,
        currentRegion: RegionId,
        quests: List<Quest>,
        openedChests: Set<String>,
        defeatedBosses: Set<String>,
        unlockedWaypoints: Set<String>
    ) {
        val root = JSONObject()
        root.put("timestamp", System.currentTimeMillis())
        root.put("playerClass", player.playerClass.name)
        root.put("x", player.x.toDouble())
        root.put("y", player.y.toDouble())
        root.put("currentRegion", currentRegion.name)

        // Stats
        root.put("level", player.stats.level)
        root.put("xp", player.stats.xp)
        root.put("xpToNext", player.stats.xpToNext)
        root.put("statPoints", player.stats.statPoints)
        root.put("skillPoints", player.stats.skillPoints)
        root.put("strength", player.stats.strength)
        root.put("agility", player.stats.agility)
        root.put("intelligence", player.stats.intelligence)
        root.put("vitality", player.stats.vitality)
        root.put("maxHp", player.stats.maxHp.toDouble())
        root.put("currentHp", player.stats.currentHp.toDouble())
        root.put("maxMana", player.stats.maxMana.toDouble())
        root.put("currentMana", player.stats.currentMana.toDouble())
        root.put("maxStamina", player.stats.maxStamina.toDouble())
        root.put("currentStamina", player.stats.currentStamina.toDouble())
        root.put("gold", player.stats.gold)

        // Equipment IDs
        player.equippedWeapon?.let { root.put("eq_weapon", it.id) }
        player.equippedShield?.let { root.put("eq_shield", it.id) }
        player.equippedArmor?.let { root.put("eq_armor", it.id) }
        player.equippedHelmet?.let { root.put("eq_helmet", it.id) }
        player.equippedBoots?.let { root.put("eq_boots", it.id) }
        player.equippedRing?.let { root.put("eq_ring", it.id) }

        // Inventory array
        val invArray = JSONArray()
        for (item in player.inventory) {
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("quantity", item.quantity)
            invArray.put(itemObj)
        }
        root.put("inventory", invArray)

        // Skills
        val skillsArray = JSONArray()
        for (s in player.unlockedSkills) {
            skillsArray.put(s)
        }
        root.put("unlockedSkills", skillsArray)

        // Quests
        val questsArray = JSONArray()
        for (q in quests) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("currentCount", q.currentCount)
            qObj.put("isCompleted", q.isCompleted)
            qObj.put("isClaimed", q.isClaimed)
            questsArray.put(qObj)
        }
        root.put("quests", questsArray)

        // World flags
        val chestsArray = JSONArray()
        openedChests.forEach { chestsArray.put(it) }
        root.put("openedChests", chestsArray)

        val bossesArray = JSONArray()
        defeatedBosses.forEach { bossesArray.put(it) }
        root.put("defeatedBosses", bossesArray)

        val waypointsArray = JSONArray()
        unlockedWaypoints.forEach { waypointsArray.put(it) }
        root.put("unlockedWaypoints", waypointsArray)

        prefs.edit().putString("save_slot_$slotIndex", root.toString()).apply()
    }

    fun loadGame(slotIndex: Int): LoadedSaveData? {
        val jsonStr = prefs.getString("save_slot_$slotIndex", null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val pClassName = root.optString("playerClass", "KNIGHT")
            val pClass = try { PlayerClass.valueOf(pClassName) } catch (_: Exception) { PlayerClass.KNIGHT }

            val stats = PlayerStats(
                level = root.optInt("level", 1),
                xp = root.optInt("xp", 0),
                xpToNext = root.optInt("xpToNext", 100),
                statPoints = root.optInt("statPoints", 0),
                skillPoints = root.optInt("skillPoints", 0),
                strength = root.optInt("strength", 10),
                agility = root.optInt("agility", 10),
                intelligence = root.optInt("intelligence", 10),
                vitality = root.optInt("vitality", 10),
                maxHp = root.optDouble("maxHp", 120.0).toFloat(),
                currentHp = root.optDouble("currentHp", 120.0).toFloat(),
                maxMana = root.optDouble("maxMana", 50.0).toFloat(),
                currentMana = root.optDouble("currentMana", 50.0).toFloat(),
                maxStamina = root.optDouble("maxStamina", 100.0).toFloat(),
                currentStamina = root.optDouble("currentStamina", 100.0).toFloat(),
                gold = root.optInt("gold", 150)
            )

            val player = Player(
                playerClass = pClass,
                x = root.optDouble("x", 400.0).toFloat(),
                y = root.optDouble("y", 400.0).toFloat(),
                stats = stats
            )

            // Equipment lookup helper
            fun findItem(id: String): Item? {
                val catalog = listOf(
                    ItemCatalog.RUSTY_SWORD, ItemCatalog.STEEL_BROADSWORD, ItemCatalog.SHADOW_DAGGERS,
                    ItemCatalog.ELVEN_LONGBOW, ItemCatalog.ARCANE_STAFF, ItemCatalog.VOIDBRINGER_GREATSWORD,
                    ItemCatalog.WOODEN_BUCKLER, ItemCatalog.TOWER_SHIELD, ItemCatalog.AEGIS_OF_LIGHT,
                    ItemCatalog.CLOTH_TUNIC, ItemCatalog.CHAINMAIL_VEST, ItemCatalog.DRAGONSCALE_ARMOR,
                    ItemCatalog.IRON_HELM, ItemCatalog.CROWN_OF_SOULS, ItemCatalog.LEATHER_BOOTS,
                    ItemCatalog.WINGED_GREAVES, ItemCatalog.RING_VITALITY, ItemCatalog.RING_BERSERKER,
                    ItemCatalog.HEALTH_POTION, ItemCatalog.MANA_POTION, ItemCatalog.STAMINA_ELIXIR,
                    ItemCatalog.GREATER_HEALING, ItemCatalog.IRON_ORE, ItemCatalog.WOLF_PELT,
                    ItemCatalog.MONSTER_BONE, ItemCatalog.VOID_ESSENCE, ItemCatalog.HEALING_HERB,
                    ItemCatalog.CRYPT_KEY, ItemCatalog.ANCIENT_TABLET
                )
                return catalog.find { it.id == id }
            }

            root.optString("eq_weapon", null)?.let { player.equippedWeapon = findItem(it) }
            root.optString("eq_shield", null)?.let { player.equippedShield = findItem(it) }
            root.optString("eq_armor", null)?.let { player.equippedArmor = findItem(it) }
            root.optString("eq_helmet", null)?.let { player.equippedHelmet = findItem(it) }
            root.optString("eq_boots", null)?.let { player.equippedBoots = findItem(it) }
            root.optString("eq_ring", null)?.let { player.equippedRing = findItem(it) }

            // Inventory
            val invArray = root.optJSONArray("inventory")
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    val obj = invArray.getJSONObject(i)
                    val id = obj.optString("id")
                    val qty = obj.optInt("quantity", 1)
                    findItem(id)?.let { item ->
                        player.inventory.add(item.copy(quantity = qty))
                    }
                }
            }

            // Skills
            val skillsArray = root.optJSONArray("unlockedSkills")
            if (skillsArray != null) {
                for (i in 0 until skillsArray.length()) {
                    player.unlockedSkills.add(skillsArray.getString(i))
                }
            }

            val regionName = root.optString("currentRegion", "OAKWOOD_VILLAGE")
            val regionId = try { RegionId.valueOf(regionName) } catch (_: Exception) { RegionId.OAKWOOD_VILLAGE }

            val openedChests = mutableSetOf<String>()
            val cArray = root.optJSONArray("openedChests")
            if (cArray != null) {
                for (i in 0 until cArray.length()) openedChests.add(cArray.getString(i))
            }

            val defeatedBosses = mutableSetOf<String>()
            val bArray = root.optJSONArray("defeatedBosses")
            if (bArray != null) {
                for (i in 0 until bArray.length()) defeatedBosses.add(bArray.getString(i))
            }

            val waypoints = mutableSetOf<String>()
            val wArray = root.optJSONArray("unlockedWaypoints")
            if (wArray != null) {
                for (i in 0 until wArray.length()) waypoints.add(wArray.getString(i))
            }

            LoadedSaveData(
                player = player,
                currentRegion = regionId,
                openedChests = openedChests,
                defeatedBosses = defeatedBosses,
                unlockedWaypoints = waypoints
            )
        } catch (_: Exception) {
            null
        }
    }

    fun hasAnySave(): Boolean {
        for (i in 1..3) {
            if (prefs.contains("save_slot_$i")) return true
        }
        return false
    }
}

data class LoadedSaveData(
    val player: Player,
    val currentRegion: RegionId,
    val openedChests: Set<String>,
    val defeatedBosses: Set<String>,
    val unlockedWaypoints: Set<String>
)
