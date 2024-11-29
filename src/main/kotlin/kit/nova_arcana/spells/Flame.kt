package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.entities.FireballProj
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger


fun regFlame(logger: Logger) {
    registerSpell(
        Identifier("nova_arcana:spell/flame"),
        Text.literal("Flame"),
        Identifier("nova_arcana:item/mat-flame")
    ) { wld, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.FIREONLY)) {
                if (wld.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                if (wld.isClient) {
                    wld.playSound(
                        user,
                        user.blockPos,
                        SoundEvent.of(Identifier("item.firecharge.use"), 10.0F),
                        SoundCategory.PLAYERS
                    )
                    return@run SpellCastResult.SUCCESS
                }
                h.mana -= cost
            } else return@run SpellCastResult.FAIL

            wld.playSound(
                user,
                user.blockPos,
                SoundEvent.of(Identifier("item.firecharge.use"), 10.0F),
                SoundCategory.PLAYERS
            )
            for (i in 0..10) {
                val bolt = FireballProj(ModEntities.FireballProjType, user, wld)
                bolt.setNoGravity(true)
                bolt.setPosition(user.eyePos)
                if (mod == SpellMod.PWR) bolt.burntime = 6
                val maxLifespan = if (mod == SpellMod.AREA) -10 else 0
                bolt.setLifespan((maxLifespan..5).random())
                bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 1F, 20F)
                wld.spawnEntity(bolt)
            }
            h.syncMana()
            SpellCastResult.SUCCESS
        }
    }
}