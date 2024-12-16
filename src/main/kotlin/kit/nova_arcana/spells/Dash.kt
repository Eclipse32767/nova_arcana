package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import kit.nova_arcana.mixin.InvAccessor
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger

fun regDash(logger: Logger) {
    registerSpell(Identifier("nova_arcana:spell/dash"), Text.literal("Dash"), Identifier("nova_arcana:item/mat-dash")) { world, user, hand, mod ->
        run {
            if (user.isOnGround) return@run SpellCastResult.FAIL
            val cost = if (mod == SpellMod.EFF) 40 else 60
            val speed = if (mod == SpellMod.AREA) 2.5f else 1.5f
            val inv = if (mod == SpellMod.PWR) 15 else 10
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.AIRONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                h.mana -= cost
                h.syncMana()
            } else return@run SpellCastResult.FAIL
            launchPlayer(user, user.pitch, user.yaw, 0.0f, speed, 0.0f)
            for (i in 0..20) {
                val deviationX = (-10..10).random().toDouble() / 10
                val deviationY = (-10..10).random().toDouble() / 10 + 1
                val deviationZ = (-10..10).random().toDouble() / 10
                if (world.isClient) dashParticle(1.0f, user.pos.add(deviationX, deviationY, deviationZ)).spawn(world)
            }
            user.timeUntilRegen = inv
            (user as InvAccessor).lastDamageTaken = 20.0f
            return@run SpellCastResult.SUCCESS
        }
    }
}