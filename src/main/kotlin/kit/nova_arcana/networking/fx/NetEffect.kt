package kit.nova_arcana.networking.fx

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

interface NetEffect {
    fun sendTo(user: ServerPlayerEntity);
}
fun PacketByteBuf.writeVec3d(pos: Vec3d) {
    writeDouble(pos.x)
    writeDouble(pos.y)
    writeDouble(pos.z)
}
fun PacketByteBuf.readVec3d(): Vec3d {
    return Vec3d(readDouble(), readDouble(), readDouble())
}