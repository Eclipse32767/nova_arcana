package kit.nova_arcana

import kit.nova_arcana.fx.OutlineEffects
import kit.nova_arcana.fx.RingEffects
import kit.nova_arcana.networking.fx.OutlineNetEffect
import kit.nova_arcana.networking.fx.RingNetEffect
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.util.Identifier

object ModS2CMessages {
    val RING_ID = Identifier("nova_arcana:spawn_ring")
    val OUTLINE_ID = Identifier("nova_arcana:spawn_outline")
    fun regHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(RING_ID) { client, handler, buf, responseSender ->
            val dat = RingNetEffect.fromBuf(buf)
            client.execute {
                val wld = client.world?: return@execute
                val fx = RingEffects(dat.col1, dat.col2, dat.lifespan, dat.density, dat.startScale, dat.pos, dat.rad)
                fx.motion = dat.motion
                fx.spawn(wld)
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(OUTLINE_ID) {client, handler, buf, responseSender ->
            val dat = OutlineNetEffect.fromBuf(buf)
            client.execute {
                val wld = client.world?: return@execute
                val st = if (dat.full) ModBlocks.SUPPORT_BLOCK.defaultState else wld.getBlockState(dat.pos)
                val fx = OutlineEffects(dat.col1, dat.col2, dat.density, dat.lifespan, dat.startScale, st, dat.pos)
                fx.spawn(wld)
            }
        }
    }
}