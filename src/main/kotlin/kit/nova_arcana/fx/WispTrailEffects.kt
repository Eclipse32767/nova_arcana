package kit.nova_arcana.fx

import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class WispTrailEffects(
    var smoke: Boolean,
    var col1: Color,
    var col2: Color,
    var startScale: Float,
    var lifespan: Int,
    var pos: Vec3d
): ParticleEffect {
    constructor(col1: Color, col2: Color, pos: Vec3d): this(false, col1, col2, 0.5f, 40, pos)
    var motion = Vec3d.ZERO
    override fun spawn(world: World) {
        val spawner = WorldParticleBuilder.create(if (smoke) LodestoneParticleRegistry.SMOKE_PARTICLE else LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(startScale, 0.0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(col1, col2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(lifespan)
        spawner.enableNoClip()
        spawner.setMotion(motion)
        spawner.spawn(world, pos.x, pos.y, pos.z)
    }
    fun withMotion(v: Vec3d): WispTrailEffects {
        this.motion = v
        return this
    }
}