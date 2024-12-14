package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModBlocks
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import java.util.logging.Logger

class RitualImprisoned(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualImprisonedBlockEntity(pos, state)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)
        val entity = world.getBlockEntity(pos)
        if (entity !is RitualImprisonedBlockEntity) return
        for (chunk in entity.barrierBlocks) {
            for (block in chunk) {
                if (world.getBlockState(block).isOf(entity.barrierBlk)) {
                    world.setBlockState(block, Blocks.AIR.defaultState)
                }
            }
        }
    }
    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is RitualImprisonedBlockEntity) entity.tick(world, pos, state)}
    }
}

class RitualImprisonedBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.RIT_PRISON_TYPE, pos, state) {
    var tickCount = 0
    val range = 10
    val innerRange = range-1
    val barrierBlk = ModBlocks.BARRIER
    val logger = LoggerFactory.getLogger("rit-prison")
    val rangeBlocks = lazyCartesianProduct(pos.x-range..pos.x+range,
        pos.y-range..pos.y+range,
        pos.z-range..pos.z+range
    ).map { BlockPos(it.first, it.second, it.third) }
    val barrierBlocks = rangeBlocks.filter { !devRange(pos.x, innerRange).contains(it.x) || !devRange(pos.y, innerRange).contains(it.y) || !devRange(pos.z, innerRange).contains(it.z) }.shuffled().chunked(40).toList()
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (world.isClient) return
        tickCount++
        //if (tickCount % 60 == 0) return
        val location = tickCount % barrierBlocks.size
        var breakBarrier = false
        if (world.isReceivingRedstonePower(pos)) breakBarrier = true
        if (breakBarrier) {
            for (maybeBarrier in barrierBlocks[location]) {
                //if ((1..4).random() != 1) continue
                //if (innerBlocks.contains(maybeBarrier)) continue
                val st = world.getBlockState(maybeBarrier)
                if (st.isOf(barrierBlk)) {
                    world.setBlockState(maybeBarrier, Blocks.AIR.defaultState)
                }
            }
        } else {
            for (space in barrierBlocks[location]) {
                //if ((1..4).random() != 1) continue
                //if (innerBlocks.contains(space)) continue
                val st = world.getBlockState(space)
                if (st.isOf(Blocks.AIR)) {
                    world.setBlockState(space, barrierBlk.defaultState)
                }
            }
        }
    }
}

fun <A, B> lazyCartesianProduct(
    listA: Iterable<A>,
    listB: Iterable<B>
): Sequence<Pair<A, B>> =
    sequence {
        listA.forEach { a ->
            listB.forEach { b ->
                yield(a to b)
            }
        }
    }
fun <A, B, C> lazyCartesianProduct(
    listA: Iterable<A>,
    listB: Iterable<B>,
    listC: Iterable<C>
): Sequence<Triple<A, B, C>> =
    sequence {
        listA.forEach { a ->
            listB.forEach { b ->
                listC.forEach {c ->
                    yield(Triple(a, b, c))
                }
            }
        }
    }
private fun devRange(v: Int, deviation: Int) = v-deviation..v+deviation
