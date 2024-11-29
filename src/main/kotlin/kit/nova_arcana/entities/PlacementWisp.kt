package kit.nova_arcana.entities

import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class PlacementWisp(type: EntityType<PlacementWisp>, world: World, val dest: BlockPos, val block: BlockState): ThrownItemEntity(type, world) {
    val logger = LoggerFactory.getLogger("nova_arcana")
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun mvTowardTrgt() {
        val diff = dest.toCenterPos() - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    val particleBuilder: WorldParticleBuilder
        get() {
            val startCol = Color(5, 104, 186)
            val edCol = Color(93, 239, 252)
            val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
            spawner.scaleData = GenericParticleData.create(0.75f, 0.0f).build()
            spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
            spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
            spawner.setLifetime(40)
            spawner.enableNoClip()
            return spawner
        }
    val outlineBuilder: WorldParticleBuilder
        get() {
            val startCol = Color(5, 104, 186)
            val edCol = Color(93, 239, 252)
            val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
            spawner.scaleData = GenericParticleData.create(0.25f, 0.0f).build()
            spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
            spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
            spawner.setLifetime(40)
            spawner.enableNoClip()
            return spawner
        }
    fun spawnParticle() {
        particleBuilder.spawn(world, x, y, z)
    }
    override fun tick() {
        super.tick()
        //mvTowardTrgt()
        spawnParticle()
        if (pos.distanceTo(dest.toCenterPos()) < 1) {
            if (!world.isClient) {
                world.setBlockState(dest, block)
            } else {
                for (i in 0..5) particleBuilder.createBlockOutline(world, dest, block)
            }

            //logger.atInfo().log(dest.toString())

            kill()
        }
    }
}