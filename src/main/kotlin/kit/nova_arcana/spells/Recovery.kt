package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import org.slf4j.Logger

fun regRecovery(logger: Logger) {
    registerSpell(
        Identifier("nova_arcana:spell/recovery"), Text.literal("Recovery"), Identifier("nova_arcana:item/mat-recovery")
    ) { world, user, hand, mod ->
        run {
            val amount = if (mod == SpellMod.PWR) 2f else 1f
            val cost = if (mod == SpellMod.EFF) 20 else 40
            val area = if (mod == SpellMod.AREA) 5 else 3
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.SPIRITONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                h.mana -= cost
                h.syncMana()
            } else return@run SpellCastResult.FAIL
            user.heal(amount)
            for (entity in world.getOtherEntities(user, Box(user.x - area, user.y, user.z - area, user.x + area, user.y + 1 + area, user.z + area))) {
                if (entity is LivingEntity) {
                    entity.heal(amount)
                }
            }
            recoverParticle(0.25f, 0.0f).setRandomMotion(0.0, 0.1, 0.0).repeatCircle(world, user.x, user.y+1, user.z, area.toDouble(), 40)
            return@run SpellCastResult.SUCCESS
        }
    }
}