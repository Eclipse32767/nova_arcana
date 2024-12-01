package kit.nova_arcana.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class InfusionParticleLine(type: EntityType<InfusionParticleLine>, world: World, val color1: Color, val color2: Color, val dest: Vec3d): ThrownItemEntity(type, world) {
    fun particleSpawner(): WorldParticleBuilder {
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.75f, 0.0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(color1, color2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(40)
        spawner.enableNoClip()
        return spawner
    }
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun mvTowardTrgt() {
        val diff = dest - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    override fun tick() {
        super.tick()
        //setNoGravity(true)
        //mvTowardTrgt()
        //val logger = LoggerFactory.getLogger("hhhh")
        //logger.atInfo().log("${velocity.x}, ${velocity.y}, ${velocity.z}")
        particleSpawner().spawn(world, x, y, z)
        if (pos.distanceTo(dest) < 1.0) kill()
    }
}