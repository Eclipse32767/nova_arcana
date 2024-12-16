package kit.nova_arcana.fx

import net.minecraft.entity.boss.BossBar
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class ManaLineEffects(
    val col1: Color,
    val col2: Color,
    val lifespan: Int,
    val density: Int,
    val startScale: Float,
    val start: Vec3d,
    val dest: Vec3d
    ): ParticleEffect {
    override fun spawn(world: World) {
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(startScale, 0.0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(col1, col2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(lifespan)
        spawner.enableNoClip()
        for (i in 0..density) spawner.spawnLine(world, start, dest)
    }
}