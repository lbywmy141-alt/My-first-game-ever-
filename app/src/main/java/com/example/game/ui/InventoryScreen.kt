package com.example.game.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.*

@Composable
fun InventoryScreen(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val stats = player.stats
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50B0E14))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حقيبة العتاد والسمات",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙 ${stats.gold} ذهب",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF263238), CircleShape)
                            .testTag("close_inventory")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two Columns: Left = Character Paperdoll & Stats Allocation; Right = Inventory Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT: Character & Stats
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .background(Color(0xFF141A24), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "${player.playerClass.displayName} (المستوى ${stats.level})",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Equipment Slots (Paperdoll)
                    Text(text = "العتاد المجهز:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    EquipSlotRow("سلاح", player.equippedWeapon) { player.equippedWeapon?.let { selectedItem = it } }
                    EquipSlotRow("درع", player.equippedShield) { player.equippedShield?.let { selectedItem = it } }
                    EquipSlotRow("جوشن", player.equippedArmor) { player.equippedArmor?.let { selectedItem = it } }
                    EquipSlotRow("خوذة", player.equippedHelmet) { player.equippedHelmet?.let { selectedItem = it } }
                    EquipSlotRow("حذاء", player.equippedBoots) { player.equippedBoots?.let { selectedItem = it } }
                    EquipSlotRow("خاتم", player.equippedRing) { player.equippedRing?.let { selectedItem = it } }

                    Divider(color = Color(0xFF37474F), modifier = Modifier.padding(vertical = 8.dp))

                    // Attributes & Allocation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "السمات الأساسية:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (stats.statPoints > 0) {
                            Text(
                                text = "نقاط: ${stats.statPoints}",
                                color = Color(0xFF76FF03),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    StatRow("القوة (STR)", stats.strength, stats.statPoints > 0) {
                        stats.strength++
                        stats.statPoints--
                        engine.audioEngine.playLevelUp()
                    }
                    StatRow("الرشاقة (AGI)", stats.agility, stats.statPoints > 0) {
                        stats.agility++
                        stats.statPoints--
                        engine.audioEngine.playLevelUp()
                    }
                    StatRow("الذكاء (INT)", stats.intelligence, stats.statPoints > 0) {
                        stats.intelligence++
                        stats.statPoints--
                        engine.audioEngine.playLevelUp()
                    }
                    StatRow("الحيوية (VIT)", stats.vitality, stats.statPoints > 0) {
                        stats.vitality++
                        stats.statPoints--
                        stats.maxHp = stats.calculateMaxHp(player.playerClass.baseHp)
                        engine.audioEngine.playLevelUp()
                    }

                    Divider(color = Color(0xFF37474F), modifier = Modifier.padding(vertical = 6.dp))

                    // Total Combat Stats Summary
                    Text(text = "إجمالي الهجوم: ${player.getTotalAtk().toInt()}", color = Color(0xFFFF8A80), fontSize = 11.sp)
                    Text(text = "إجمالي الدفاع: ${player.getTotalDef().toInt()}", color = Color(0xFF82B1FF), fontSize = 11.sp)
                    Text(text = "فرصة الضربة القاضية: ${(stats.calculateCritChance(0f) * 100).toInt()}%", color = Color(0xFFFFD54F), fontSize = 11.sp)
                }

                // RIGHT: Inventory Grid & Item Details Sheet
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .background(Color(0xFF141A24), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF8B6B3D), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    // Category Filter Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CategoryTab("الكل", selectedCategory == "ALL") { selectedCategory = "ALL" }
                        CategoryTab("أسلحة", selectedCategory == "WEAPON") { selectedCategory = "WEAPON" }
                        CategoryTab("دروع", selectedCategory == "ARMOR") { selectedCategory = "ARMOR" }
                        CategoryTab("جرعات", selectedCategory == "POTION") { selectedCategory = "POTION" }
                        CategoryTab("مواد", selectedCategory == "MATERIAL") { selectedCategory = "MATERIAL" }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item Grid
                    val filteredItems = player.inventory.filter {
                        when (selectedCategory) {
                            "WEAPON" -> it.type == ItemType.WEAPON
                            "ARMOR" -> it.type in listOf(ItemType.SHIELD, ItemType.ARMOR, ItemType.HELMET, ItemType.BOOTS, ItemType.RING)
                            "POTION" -> it.type == ItemType.POTION
                            "MATERIAL" -> it.type == ItemType.MATERIAL || it.type == ItemType.QUEST_ITEM
                            else -> true
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 58.dp),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredItems) { item ->
                            val isSelected = selectedItem?.id == item.id
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .background(Color(0xFF1E2633), RoundedCornerShape(6.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color(item.rarity.colorHex),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedItem = item },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = item.icon, fontSize = 20.sp)
                                    if (item.quantity > 1) {
                                        Text(
                                            text = "x${item.quantity}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Item Action Bar (if an item is selected)
                    selectedItem?.let { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E2633), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(item.rarity.colorHex), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.name,
                                        color = Color(item.rarity.colorHex),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${item.buyPrice} ذهب",
                                        color = Color(0xFFFFD700),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = item.description,
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Equip / Unequip or Use Button
                                    if (item.isEquippable()) {
                                        val isEquipped = player.isEquipped(item)
                                        Button(
                                            onClick = {
                                                if (isEquipped) player.unequipItem(item)
                                                else player.equipItem(item)
                                                engine.audioEngine.playUiClick()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEquipped) Color(0xFFD32F2F) else Color(0xFF1976D2)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (isEquipped) "نزع العتاد" else "تجهيز",
                                                fontSize = 11.sp
                                            )
                                        }
                                    } else if (item.type == ItemType.POTION) {
                                        Button(
                                            onClick = {
                                                engine.onQuickPotion()
                                                selectedItem = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(text = "استخدام", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipSlotRow(slotName: String, item: Item?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color(0xFF1E2633), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = slotName, color = Color(0xFF90A4AE), fontSize = 10.sp)
        Text(
            text = item?.let { "${it.icon} ${it.name}" } ?: "فارغ",
            color = item?.let { Color(it.rarity.colorHex) } ?: Color(0xFF546E7A),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun StatRow(label: String, value: Int, canAdd: Boolean, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFCFD8DC), fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$value", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            if (canAdd) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF43A047), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun RowScope.CategoryTab(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(28.dp)
            .background(
                if (isSelected) Color(0xFF8B6B3D) else Color(0xFF1E2633),
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
