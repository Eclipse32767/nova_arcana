package kit.nova_arcana.fx

import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class RingEffects(
    var col1: Color,
    var col2: Color,
    var lifespan: Int,
    var density: Int,
    var startScale: Float,
    var pos: Vec3d,
    var rad: Double
): ParticleEffect {
    var motion = Vec3d.ZERO
    override fun spawn(world: World) {
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(startScale, 0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(col1, col2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(lifespan)
        spawner.enableNoClip()
        spawner.setRandomMotion(motion.x, motion.y, motion.z)
        spawner.repeatCircle(world, pos.x, pos.y, pos.z, rad, density)
    }
}