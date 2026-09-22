package com.example

import com.example.game.model.*
import org.junit.Assert.*
import org.junit.Test

class GameSystemsUnitTest {

    @Test
    fun testPlayerClassInitialization() {
        val knight = Player(playerClass = PlayerClass.KNIGHT)
        assertEquals(PlayerClass.KNIGHT, knight.playerClass)
        assertEquals(1, knight.stats.level)
        assertTrue(knight.stats.maxHp >= 100f)

        val mage = Player(playerClass = PlayerClass.MAGE)
        assertEquals(PlayerClass.MAGE, mage.playerClass)

        val archer = Player(playerClass = PlayerClass.ARCHER)
        assertEquals(PlayerClass.ARCHER, archer.playerClass)
    }

    @Test
    fun testEquipmentAndCombatStats() {
        val player = Player(playerClass = PlayerClass.KNIGHT)
        val initialAtk = player.getTotalAtk()
        val initialDef = player.getTotalDef()

        player.equipItem(ItemCatalog.STEEL_BROADSWORD)
        player.equipItem(ItemCatalog.CHAINMAIL_VEST)
        player.equipItem(ItemCatalog.TOWER_SHIELD)

        assertTrue("Attack should increase with weapon", player.getTotalAtk() > initialAtk)
        assertTrue("Defense should increase with armor and shield", player.getTotalDef() > initialDef)
        assertTrue(player.isEquipped(ItemCatalog.STEEL_BROADSWORD))

        player.unequipItem(ItemCatalog.STEEL_BROADSWORD)
        assertFalse(player.isEquipped(ItemCatalog.STEEL_BROADSWORD))
    }

    @Test
    fun testXpProgressionAndLevelUp() {
        val player = Player(playerClass = PlayerClass.KNIGHT)
        assertEquals(1, player.stats.level)

        val leveledUp = player.stats.addXp(120)
        assertTrue(leveledUp)
        assertEquals(2, player.stats.level)
        assertTrue(player.stats.statPoints > 0)
        assertTrue(player.stats.skillPoints > 0)
    }

    @Test
    fun testWorldMapTilesAndPortals() {
        val village = WorldFactory.createRegion(RegionId.OAKWOOD_VILLAGE)
        assertEquals(RegionId.OAKWOOD_VILLAGE, village.id)
        assertTrue(village.widthTiles > 0)
        assertTrue(village.heightTiles > 0)
        assertTrue(village.npcs.isNotEmpty())
        assertTrue(village.portals.isNotEmpty())

        // Test boundary collision
        assertTrue("Position off-map should be blocked", village.isTileSolid(-1, -1))
    }

    @Test
    fun testQuestProgression() {
        val quests = QuestCatalog.createDefaultQuests().toMutableList()
        val mainQuest = quests.first { it.id == "mq_1_wake" }
        assertFalse(mainQuest.isCompleted)

        mainQuest.currentCount++
        if (mainQuest.currentCount >= mainQuest.requiredCount) {
            mainQuest.isCompleted = true
        }

        assertTrue(mainQuest.isCompleted)
    }
}
