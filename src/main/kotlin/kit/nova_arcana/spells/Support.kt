package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.slf4j.Logger

fun regSupport(logger: Logger) {
    registerSpell(
        Identifier("nova_arcana:spell/support"), Text.literal("Support"), Identifier("nova_arcana:item/mat-support")
    ) { world, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val reach = if (mod == SpellMod.AREA) 20.0 else 10.0

            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.FROSTONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
            } else return@run SpellCastResult.FAIL
            val cast = user.raycast(reach, 0.0f, true)
            if (cast.type == HitResult.Type.BLOCK) {
                val blockHit = cast as BlockHitResult
                val dest = blockHit.blockPos.offset(blockHit.side)
                if (world.getBlockState(dest).isAir && !world.isClient) world.setBlockState(
                    dest,
                    ModBlocks.SUPPORT_BLOCK.defaultState
                )
                if (!world.isClient) {
                    val packet = supportParticlePacket(0.25f, dest, true, 5)
                    for (plr in PlayerLookup.tracking(world as ServerWorld, dest).filter { it != user }) {
                        packet.sendTo(plr)
                    }
                    packet.sendTo(user as ServerPlayerEntity)
                }
                h.mana -= cost
                h.syncMana()
                return@run SpellCastResult.SUCCESS
            } else return@run SpellCastResult.FAIL
        }
    }
    spellInvReg[Identifier("nova_arcana:spell/support")] = SpellInv { stk, world, entity, slot, selected, mod -> run {
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val cast = entity.raycast(reach, 0.0f, true)
        if (cast.type != HitResult.Type.BLOCK) return@run
        val hit = cast as BlockHitResult
        val dest = hit.blockPos.offset(hit.side)
        if (!world.isClient) {
            val packet = supportParticlePacket(0.10f, dest, true, 0)
            for (plr in PlayerLookup.tracking(world as ServerWorld, dest).filter { it != entity }) {
                packet.sendTo(plr)
            }
            if (entity is ServerPlayerEntity) packet.sendTo(entity)
        }
    }}
}