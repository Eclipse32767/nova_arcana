package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModEntities
import kit.nova_arcana.entities.InfusionParticleLine
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.awt.Color

class RitualHoard(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualHoardBlockEntity(pos, state)
    }
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is RitualHoardBlockEntity) entity.tick(world, pos, state)}
    }
}

class RitualHoardBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.RIT_HOARD_TYPE, pos, state) {
    var count = 0
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (world.isClient) return
        if (world.isReceivingRedstonePower(pos)) return
        count++
        if (count % 8 != 0) return
        val example = world.getBlockEntity(pos.add(0, 1, 0))
        val deposit = ItemStorage.SIDED.find(world, pos.add(0, -1, 0), Direction.UP)
        val take = mutableListOf<Pair<Storage<ItemVariant>, BlockPos>>()
        for (x in pos.x-5..pos.x+5) for (y in pos.y-5..pos.y+5) for (z in pos.z-5..pos.z+5) {
            if (x == pos.x && z == pos.z) continue
            val selected = BlockPos(x, y, z)
            if (world.getBlockEntity(selected) is PedestalEntity) continue
            val s = ItemStorage.SIDED.find(world, selected, Direction.DOWN)
            if (s != null) take += Pair(s, selected)
        }
        val transaction = Transaction.openOuter()
        if (example is PedestalEntity && deposit != null && transaction != null) {
            if (!example.stack.isEmpty) for (i in take) {
                if (StorageUtil.move(i.first, deposit, {it.matches(example.inv)}, 1, transaction) != 0L) {
                    val line = InfusionParticleLine(ModEntities.InfusionParticleType, world, pos.toCenterPos())
                    line.color1 = Color.CYAN
                    line.color2 = Color.BLUE
                    line.startScale = 0.30f
                    line.setPosition(i.second.toCenterPos())
                    line.setItem(example.inv.copy())
                    line.setNoGravity(true)
                    line.mvTowardTrgt()
                    world.spawnEntity(line)
                    transaction.commit()
                    break
                }
            }
        }
        transaction.close()
    }
}