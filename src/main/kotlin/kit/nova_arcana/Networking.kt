package kit.nova_arcana

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object ModMessages {
    val SPELLSWAP_ID = Identifier("nova_arcana:spellswap")
    val SPELLSWAP = ServerPlayNetworking.registerGlobalReceiver(SPELLSWAP_ID
    ) { server, player, nethandler, buf, sender -> run {
        for (item in player.handItems) {
            if (item.isOf(ModItems.wand)) {
                val world = player.world
                world.playSound(player, player.blockPos, SoundEvent.of(Identifier("block.piston.extend"), 10.0F), SoundCategory.PLAYERS)
                val nbt = item.orCreateNbt
                var spell = nbt.getInt("spell")
                if (!player.isSneaking) {
                    spell++
                    if (spell > ModItems.wand.spellMax(item)) spell = 0
                } else {
                    spell--
                    if (spell < 0) spell = ModItems.wand.spellMax(item)
                }

                nbt.putInt("spell", spell)
                return@run
            }
        }
    }}
}