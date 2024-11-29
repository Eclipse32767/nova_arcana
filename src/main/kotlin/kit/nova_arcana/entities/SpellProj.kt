package kit.nova_arcana.entities

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d



operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private operator fun BlockPos.plus(scalar: Int): BlockPos {
    return BlockPos(x+scalar, y+scalar, z+scalar)
}

private operator fun BlockPos.minus(scalar: Int): BlockPos {
    return BlockPos(x - scalar, y - scalar, z - scalar)
}
private operator fun BlockPos.minus(o: BlockPos): BlockPos {
    return BlockPos(x - o.x, y - o.y, z - o.z)
}