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
                if (e is LivingEntity) {
                    e.addStatusEffect(StatusEffectInstance(ModEffects.FAIR_GROUND, 10, 0))
                }
            }
        }
    }
}