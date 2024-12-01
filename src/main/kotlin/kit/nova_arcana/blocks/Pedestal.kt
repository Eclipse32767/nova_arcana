package kit.nova_arcana.blocks

import kit.nova_arcana.ImplementedInventory
import kit.nova_arcana.ModBlockEntities
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SingleStackInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.Supplier

class Pedestal(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return PedestalEntity(pos, state)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val entity = world.getBlockEntity(pos)
        if (entity !is PedestalEntity) return ActionResult.PASS
        val oldStk = entity.getStack(0).copy()
        val newStk = player.getStackInHand(hand)
        entity.setStack(0, newStk.copyAndEmpty())
        player.setStackInHand(hand, oldStk)
        return ActionResult.SUCCESS

        //return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        super.onBreak(world, pos, state, player)
        if (world.isClient) return
        val entity = world.getBlockEntity(pos)
        if (entity !is PedestalEntity) return
        val item = ItemEntity(EntityType.ITEM, world)
        item.stack = entity.getStack(0)
        item.setPosition(pos.toCenterPos())
        world.spawnEntity(item)
    }
}
class PedestalEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.PEDESTAL_TYPE, pos, state), SingleStackInventory {
    var inv = ItemStack.EMPTY

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.contains("item")) {
            inv = ItemStack.fromNbt(nbt.getCompound("item"))
        }
    }

    override fun markDirty() {
        world?.updateListeners(pos, cachedState, cachedState, 3)
        super.markDirty()
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.put("item", inv.writeNbt(NbtCompound()))
    }
    override fun getStack(slot: Int): ItemStack {
        return inv
    }
    override fun getStack(): ItemStack {
        return inv
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        val itm = inv.copyAndEmpty()
        markDirty()
        return itm
    }

    override fun removeStack(amount: Int): ItemStack {
        return removeStack(0, amount)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        inv = stack
        markDirty()
    }

    override fun setStack(stack: ItemStack) {
        setStack(0, stack)
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return createNbt()
    }
}