package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModEffects
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color


class RitualBlock(settings: FabricBlockSettings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualBlockEntity(pos, state)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is RitualBlockEntity) RitualBlockEntity.tick(world, pos, state, entity)}
    }
}

class RitualBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(ModBlockEntities.RITUAL_TYPE, pos, state) {

    companion object {
        @JvmStatic
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: RitualBlockEntity) {
            for (e in world.getOtherEntities(null, Box.of(pos.toCenterPos(), 20.0, 20.0, 20.0))) {
                val startCol = Color(100, 0, 100)
                val edCol = Color(0, 100, 200)
                val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                spawner.scaleData = GenericParticleData.create(0.1f, 0F).build()
                spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
                spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
                //spawner.spinData = SpinParticleData.create(0.2f, 0.4f).setSpinOffset((world.time * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build()
                spawner.setLifetime(40)
                //spawner.setRandomMotion(0.0, 0.1, 0.0)
                spawner.enableNoClip()
                spawner.spawnLine(world, pos.toCenterPos(), e.pos)
                if (e is LivingEntity) {
                    e.addStatusEffect(StatusEffectInstance(ModEffects.FAIR_GROUND, 10, 0))
                }
            }
        }
    }
}