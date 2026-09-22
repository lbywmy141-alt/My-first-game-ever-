package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.ItemCatalog

@Composable
fun ShopDialog(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val shopItems = remember {
        listOf(
            ItemCatalog.STEEL_BROADSWORD,
            ItemCatalog.TOWER_SHIELD,
            ItemCatalog.CHAINMAIL_VEST,
            ItemCatalog.HEALTH_POTION.copy(quantity = 1),
            ItemCatalog.MANA_POTION.copy(quantity = 1),
            ItemCatalog.STAMINA_ELIXIR.copy(quantity = 1),
            ItemCatalog.GREATER_HEALING.copy(quantity = 1)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF141A24), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "متجر التاجر زاريك 🛒",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙 ${player.stats.gold} ذهب",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF263238), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shopItems) { item ->
                    val canAfford = player.stats.gold >= item.buyPrice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2633), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(item.rarity.colorHex), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color(item.rarity.colorHex),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(text = item.description, color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                if (canAfford) {
                                    player.stats.gold -= item.buyPrice
                                    val existing = player.inventory.firstOrNull { it.id == item.id }
                                    if (existing != null) existing.quantity++
                                    else player.inventory.add(item.copy(quantity = 1))
                                    engine.audioEngine.playCoin()
                                }
                            },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                disabledContainerColor = Color(0xFF37474F)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "شراء (${item.buyPrice} ذهب)",
                                color = if (canAfford) Color.Black else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CraftingDialog(
    engine: GameEngine,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val recipes = remember { ItemCatalog.RECIPES }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xBB000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .background(Color(0xFF141A24), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF8B6B3D), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سندان الحداد بروم ⚒️",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF263238), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes) { recipe ->
                    // Check if player has materials
                    val hasIngredients = recipe.requiredMaterials.all { (matId, reqQty) ->
                        val item = player.inventory.firstOrNull { it.id == matId }
                        (item?.quantity ?: 0) >= reqQty
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2633), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(recipe.resultItem.rarity.colorHex), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = recipe.resultItem.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recipe.resultItem.name,
                                color = Color(recipe.resultItem.rarity.colorHex),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            val matsText = recipe.requiredMaterials.entries.joinToString(", ") { "${it.key} x${it.value}" }
                            Text(text = "المواد: $matsText", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                if (hasIngredients) {
                                    // Deduct materials
                                    for ((matId, reqQty) in recipe.requiredMaterials) {
                                        val item = player.inventory.firstOrNull { it.id == matId }
                                        if (item != null) {
                                            item.quantity -= reqQty
                                            if (item.quantity <= 0) player.inventory.remove(item)
                                        }
                                    }
                                    // Add result
                                    val existing = player.inventory.firstOrNull { it.id == recipe.resultItem.id }
                                    if (existing != null) existing.quantity++
                                    else player.inventory.add(recipe.resultItem.copy(quantity = 1))

                                    engine.audioEngine.playHeavyHit()
                                    engine.particleSystem.spawnHitSparks(player.x, player.y, 16, Color(0xFFFFD700))
                                }
                            },
                            enabled = hasIngredients,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                disabledContainerColor = Color(0xFF37474F)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "صناعة",
                                color = if (hasIngredients) Color.Black else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
