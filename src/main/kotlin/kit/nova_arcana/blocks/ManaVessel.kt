package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.awt.Color

enum class ManaFilter(val a: Color, val b: Color) {
    FIRE(Color.RED, Color.ORANGE),
    ICE(Color.BLUE, Color.CYAN),
    WIND(Color.YELLOW, Color.LIGHT_GRAY),
    EARTH(Color.GREEN, Color.LIGHT_GRAY),
    SPIRIT(Color.WHITE, Color.PINK),
    VOID(Color.MAGENTA, Color.BLUE);
}

class ManaVessel(settings: Settings, val filter: ManaFilter) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return ManaVesselEntity(pos, state, filter)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        //if (world.isClient) return ActionResult.SUCCESS
        val entity = world.getBlockEntity(pos)
        if (entity is ManaVesselEntity) player.sendMessage(Text.literal("This vessel is holding ${entity.contents}, ${entity.manaFilter} mana"))
        return ActionResult.SUCCESS
        //return super.onUse(state, world, pos, player, hand, hit)
    }
}

class ManaVesselEntity(pos: BlockPos, state: BlockState, filter: ManaFilter): BlockEntity(ModBlockEntities.MANA_VESSEL_TYPE, pos, state) {
    var manaFilter = filter
    var contents = 0
    var max = 400
    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        contents = nbt.getInt("contents")
        max = nbt.getInt("max")
        markDirty()
        manaFilter = ManaFilter.entries.toTypedArray()[nbt.getInt("filter")]
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("contents", contents)
        nbt.putInt("max", max)
        nbt.putInt("filter", manaFilter.ordinal)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return createNbt()
    }

    fun add(t: ManaFilter, v: Int): Int {
        if (t != manaFilter) return v
        val newContents = contents + v
        if (newContents > max) {
            val out = newContents - max
            contents = max
            markDirty()
            return out
        }
        contents = newContents
        markDirty()
        return 0
    }
    fun sub(t: ManaFilter, v: Int): Int {
        if (t != manaFilter) return 0
        val newContents = contents - v
        if (newContents < 0) {
            val out = contents
            contents = 0
            markDirty()
            return out
        }
        contents = newContents
        markDirty()
        return v
    }

    override fun markDirty() {
        world?.updateListeners(pos, cachedState, cachedState, 3)
        super.markDirty()
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }
}