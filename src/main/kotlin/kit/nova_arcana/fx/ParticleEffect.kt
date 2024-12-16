package kit.nova_arcana.fx

import net.minecraft.world.World

fun interface ParticleEffect {
    fun spawn(world: World);
}