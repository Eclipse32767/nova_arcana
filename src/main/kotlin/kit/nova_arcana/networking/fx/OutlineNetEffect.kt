package kit.nova_arcana.networking.fx

import kit.nova_arcana.ModS2CMessages
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import java.awt.Color

class OutlineNetEffect(
    val col1: Color,
    val col2: Color,
    val density: Int,
    val lifespan: Int,
    val startScale: Float,
    val full: Boolean,
    val pos: BlockPos
): NetEffect {
    override fun sendTo(user: ServerPlayerEntity) {
        val buf = PacketByteBufs.create()
        buf.writeInt(col1.rgb)
        buf.writeInt(col2.rgb)
        buf.writeInt(density)
        buf.writeInt(lifespan)
        buf.writeFloat(startScale)
        buf.writeBoolean(full)
        buf.writeBlockPos(pos)
        ServerPlayNetworking.send(user, ModS2CMessages.OUTLINE_ID, buf)
    }
    companion object {
        fun fromBuf(buf: PacketByteBuf): OutlineNetEffect {
            return OutlineNetEffect(
                Color(buf.readInt()),
                Color(buf.readInt()),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readBlockPos()
            )
        }
    }
}