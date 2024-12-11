package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModEntities
import kit.nova_arcana.entities.ManaBeam
import kit.nova_arcana.mixin.BurnTimeAccessor
import kit.nova_arcana.recipes.ManaOutputs
import net.minecraft.block.*
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class RitualForge(settings: Settings?) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualForgeBlockEntity(pos, state)
    }
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is RitualForgeBlockEntity) entity.tick(world, pos, state)}
    }
}
class RitualForgeBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.RIT_FORGE_TYPE, pos, state) {
    val reach = 2
    val manacost = ManaOutputs(10, 0, 0, 0, 5, 0)
    private val logger = LoggerFactory.getLogger("nova_arcana")
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (world.isClient) return
        if (world.isReceivingRedstonePower(pos)) return
        val manaPool = mutableListOf<ManaVesselEntity>()
        for (x in -10..10) for (y in -3..3) for (z in -10..10) {
            val vessel = world.getBlockEntity(BlockPos(pos.x + x, pos.y + y, pos.z + z))
            if (vessel is ManaVesselEntity) manaPool += vessel
        }
        val lineSpawned = mutableListOf<BlockPos>()
        for (x in pos.x-reach..pos.x+reach) for (y in pos.y-reach..pos.y+reach) for (z in pos.z-reach..pos.z+reach) {
            val entity = world.getBlockEntity(BlockPos(x, y, z))
            if (entity is BurnTimeAccessor && entity is AbstractFurnaceBlockEntity) {
                if (entity.burnTime < 10 && !entity.getStack(0).isEmpty) {
                    val manaNeeds = manacost.pairList()
                    for (need in manaNeeds) {
                        var amtMet = need.second
                        for (vessel in manaPool) {
                            val subtracted = vessel.sub(need.first, amtMet)
                            vessel.markDirty()
                            amtMet -= subtracted
                            if (subtracted <= 0) continue
                            if (lineSpawned.contains(vessel.pos)) {
                                logger.atInfo().log("already spawned from here this tick")
                                continue
                            }
                            logger.atInfo().log("spawning beam at ${vessel.pos.x}, ${vessel.pos.y}, ${vessel.pos.z}")
                            val line = ManaBeam(ModEntities.ManaBeamType, world)
                            line.color1 = need.first.a
                            line.color2 = need.first.b
                            line.startScale = 0.20f
                            line.dest = pos.toCenterPos()
                            line.setPosition(vessel.pos.toCenterPos())
                            line.setNoGravity(true)
                            //line.mvTowardTrgt()
                            world.spawnEntity(line)
                            lineSpawned += vessel.pos
                            logger.atInfo().log(lineSpawned.toString())
                        }
                        if (amtMet > 0) return
                    }
                    entity.burnTime = 1600
                    entity.fuelTime = 1600
                    var st = world.getBlockState(BlockPos(x, y, z))
                    st = st.with(AbstractFurnaceBlock.LIT, true)
                    world.setBlockState(BlockPos(x, y, z), st, Block.NOTIFY_ALL)
                }
            }
        }
    }
}