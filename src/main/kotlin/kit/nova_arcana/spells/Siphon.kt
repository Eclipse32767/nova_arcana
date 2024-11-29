package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.entities.DrainBeam
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger

fun regSiphon(logger: Logger) {
    registerSpell(
        Identifier("nova_arcana:spell/siphon"), Text.literal("Siphon"), Identifier("nova_arcana:item/mat-siphon")
    ) { world, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.VOIDONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                if (world.isClient) {
                    return@run SpellCastResult.SUCCESS
                }
                h.mana -= cost
            } else return@run SpellCastResult.FAIL
            val bolt = DrainBeam(ModEntities.DrainBeamType, user, world)
            bolt.setNoGravity(true)
            bolt.setPosition(user.eyePos)
            if (mod == SpellMod.AREA) bolt.pierce = 2
            if (mod == SpellMod.PWR) bolt.dmg = 6F
            bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 1F, 0F)
            world.spawnEntity(bolt)
            h.syncMana()
            SpellCastResult.SUCCESS
        }
    }
}