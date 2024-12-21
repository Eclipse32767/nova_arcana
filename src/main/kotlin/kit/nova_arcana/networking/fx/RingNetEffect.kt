package kit.nova_arcana.networking.fx

import kit.nova_arcana.ModS2CMessages
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.awt.Color


class RingNetEffect(
    var col1: Color,
    var col2: Color,
    var lifespan: Int,
    var density: Int,
    var startScale: Float,
    var pos: Vec3d,
    var rad: Double,
    var motion: Vec3d
): NetEffect {
    override fun sendTo(user: ServerPlayerEntity) {
        val buf = PacketByteBufs.create()
        buf.writeInt(col1.rgb)
        buf.writeInt(col2.rgb)
        buf.writeInt(lifespan)
        buf.writeInt(density)
        buf.writeFloat(startScale)
        buf.writeVec3d(pos)
        buf.writeDouble(rad)
        buf.writeVec3d(motion)
        ServerPlayNetworking.send(user, ModS2CMessages.RING_ID, buf)
    }
    companion object {
        fun fromBuf(buf: PacketByteBuf): RingNetEffect {
            return RingNetEffect(
                Color(buf.readInt()),
                Color(buf.readInt()),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readVec3d(),
                buf.readDouble(),
                buf.readVec3d()
            )
        }
    }
}