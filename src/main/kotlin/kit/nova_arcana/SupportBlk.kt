package kit.nova_arcana

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FrostedIceBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SupportBlk(settings: Settings) : FrostedIceBlock(settings) {
    override fun melt(state: BlockState, world: World, pos: BlockPos) {
        world.setBlockState(pos, Blocks.AIR.defaultState)
        world.updateNeighbor(pos, Blocks.AIR, pos)
    }

    override fun afterBreak(
        world: World?,
        player: PlayerEntity?,
        pos: BlockPos?,
        state: BlockState?,
        blockEntity: BlockEntity?,
        tool: ItemStack?
    ) {
        super.afterBreak(world, player, pos, state, blockEntity, tool)
        world!!.setBlockState(pos, Blocks.AIR.defaultState)
    }
}