package kit.nova_arcana.blocks

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.RedstoneView
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class Deconstructor(settings: FabricBlockSettings): Block(settings) {
    val logger = LoggerFactory.getLogger("deconstructor_log")
    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        sourcePos: BlockPos,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
        if (world.isClient) return
        val view = world as RedstoneView
        val pedestalPos = BlockPos(pos.x, pos.y+1, pos.z)
        val pedestal = world.getBlockEntity(pedestalPos)
        if (view.isReceivingRedstonePower(pos)) {
            logger.atInfo().log("am powered")
            if (pedestal is PedestalEntity) {
                logger.atInfo().log("pedestal above holding ${pedestal.stack.item}")
            }
        }
    }
}