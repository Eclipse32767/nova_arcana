package kit.nova_arcana.entities

import kit.nova_arcana.fx.OutlineEffects
import kit.nova_arcana.fx.WispTrailEffects
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
    val col1 = Color(5, 104, 186)
    val col2 = Color(93, 239, 252)
    val logger = LoggerFactory.getLogger("nova_arcana")
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun mvTowardTrgt() {
        val diff = dest.toCenterPos() - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    override fun tick() {
        super.tick()
        //mvTowardTrgt()
        if (world.isClient) {
            WispTrailEffects(false, col1, col2, 0.75f, 40, pos).spawn(world)
        }
        if (pos.distanceTo(dest.toCenterPos()) < 1) {
            if (!world.isClient) {
                world.setBlockState(dest, block)
            } else {
                OutlineEffects(col1, col2, 5, 40, 0.75f, world.getBlockState(dest), dest).spawn(world)
            }

            //logger.atInfo().log(dest.toString())

            kill()
        }
    }
}