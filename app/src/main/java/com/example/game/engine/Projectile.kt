package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.Direction

enum class ProjectileOwner {
    PLAYER,
    ENEMY
}

data class Projectile(
    val id: String,
    val owner: ProjectileOwner,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val damage: Float,
    val isCrit: Boolean = false,
    val radius: Float = 8f,
    val color: Color = Color.Yellow,
    var life: Float = 1.8f,
    val maxLife: Float = 1.8f,
    val effectType: String = "normal" // "fire", "ice", "void", "arrow", "shockwave"
)
