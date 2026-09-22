package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.game.model.WeatherType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleType {
    SPARK,
    BLOOD,
    SLASH_TRAIL,
    MAGIC_BURST,
    DUST,
    RAIN_DROP,
    SNOW_FLAKE,
    TORCH_EMBER,
    TEXT_DAMAGE
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color,
    val type: ParticleType,
    val text: String? = null
)

class ParticleSystem {
    val particles: MutableList<Particle> = mutableListOf()
    val damageTexts: MutableList<Particle> = mutableListOf()

    fun update(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt
            if (p.life <= 0f) {
                pIter.remove()
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                if (p.type == ParticleType.BLOOD || p.type == ParticleType.DUST) {
                    p.vx *= 0.88f
                    p.vy *= 0.88f
                } else if (p.type == ParticleType.SPARK) {
                    p.vy += 200f * dt // gravity
                }
            }
        }

        val dIter = damageTexts.iterator()
        while (dIter.hasNext()) {
            val d = dIter.next()
            d.life -= dt
            if (d.life <= 0f) {
                dIter.remove()
            } else {
                d.y += d.vy * dt
                d.x += d.vx * dt
            }
        }
    }

    fun spawnSlashArc(x: Float, y: Float, dirAngle: Float, color: Color = Color(0xFFE0E8FF)) {
        for (i in -4..4) {
            val angle = dirAngle + (i * 0.15f)
            val speed = 260f + Random.nextFloat() * 80f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.18f,
                    maxLife = 0.18f,
                    size = 5f,
                    color = color,
                    type = ParticleType.SLASH_TRAIL
                )
            )
        }
    }

    fun spawnHitSparks(x: Float, y: Float, count: Int = 10, color: Color = Color(0xFFFFCC00)) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 90f + Random.nextFloat() * 200f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.25f + Random.nextFloat() * 0.2f,
                    maxLife = 0.4f,
                    size = 4f,
                    color = color,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    fun spawnBloodSplatter(x: Float, y: Float, count: Int = 8) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 60f + Random.nextFloat() * 140f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.35f,
                    maxLife = 0.35f,
                    size = 4.5f,
                    color = Color(0xFFB71C1C),
                    type = ParticleType.BLOOD
                )
            )
        }
    }

    fun spawnMagicBurst(x: Float, y: Float, color: Color = Color(0xFF9C27B0)) {
        for (i in 0 until 16) {
            val angle = (i.toFloat() / 16f) * 2f * Math.PI.toFloat()
            val speed = 180f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0.45f,
                    maxLife = 0.45f,
                    size = 6f,
                    color = color,
                    type = ParticleType.MAGIC_BURST
                )
            )
        }
    }

    fun spawnDust(x: Float, y: Float) {
        for (i in 0 until 5) {
            particles.add(
                Particle(
                    x = x + Random.nextFloat() * 12f - 6f,
                    y = y + Random.nextFloat() * 8f - 4f,
                    vx = (Random.nextFloat() - 0.5f) * 40f,
                    vy = -Random.nextFloat() * 30f,
                    life = 0.3f,
                    maxLife = 0.3f,
                    size = 5f,
                    color = Color(0xFFC4B8A5),
                    type = ParticleType.DUST
                )
            )
        }
    }

    fun spawnDamageNumber(x: Float, y: Float, damage: Int, isCrit: Boolean = false, isPlayerDamage: Boolean = false) {
        val color = when {
            isPlayerDamage -> Color(0xFFFF5252)
            isCrit -> Color(0xFFFFD700)
            else -> Color(0xFFFFFFFF)
        }
        val text = if (isCrit) "$damage!" else "$damage"
        damageTexts.add(
            Particle(
                x = x + (Random.nextFloat() - 0.5f) * 20f,
                y = y - 20f,
                vx = (Random.nextFloat() - 0.5f) * 30f,
                vy = -65f,
                life = 0.75f,
                maxLife = 0.75f,
                size = if (isCrit) 20f else 15f,
                color = color,
                type = ParticleType.TEXT_DAMAGE,
                text = text
            )
        )
    }

    fun spawnStatusText(x: Float, y: Float, text: String, color: Color) {
        damageTexts.add(
            Particle(
                x = x,
                y = y - 25f,
                vx = 0f,
                vy = -50f,
                life = 0.9f,
                maxLife = 0.9f,
                size = 17f,
                color = color,
                type = ParticleType.TEXT_DAMAGE,
                text = text
            )
        )
    }

    fun spawnWeather(weather: WeatherType, camLeft: Float, camTop: Float, viewWidth: Float, viewHeight: Float) {
        when (weather) {
            WeatherType.RAIN, WeatherType.STORM -> {
                val count = if (weather == WeatherType.STORM) 6 else 3
                for (i in 0 until count) {
                    particles.add(
                        Particle(
                            x = camLeft + Random.nextFloat() * viewWidth,
                            y = camTop + Random.nextFloat() * 100f,
                            vx = -120f,
                            vy = 650f,
                            life = 0.6f,
                            maxLife = 0.6f,
                            size = 2.5f,
                            color = Color(0x99A0D0FF),
                            type = ParticleType.RAIN_DROP
                        )
                    )
                }
            }
            WeatherType.SNOW -> {
                for (i in 0..1) {
                    particles.add(
                        Particle(
                            x = camLeft + Random.nextFloat() * viewWidth,
                            y = camTop + Random.nextFloat() * 80f,
                            vx = (Random.nextFloat() - 0.5f) * 60f,
                            vy = 110f + Random.nextFloat() * 50f,
                            life = 1.6f,
                            maxLife = 1.6f,
                            size = 4f,
                            color = Color(0xDDFFFFFF),
                            type = ParticleType.SNOW_FLAKE
                        )
                    )
                }
            }
            else -> {}
        }
    }
}
