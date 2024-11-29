package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.entities.ImmolateProj
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger

fun regImmolate(logger: Logger) {
    registerSpell(Identifier("nova_arcana:spell/immolate"), Text.literal("Immolate"), Identifier("nova_arcana:item/mat-blank")) { world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 60 else 80
        val reach = if (mod == SpellMod.AREA) 40 else 20
        val divergence = if (mod == SpellMod.AREA) 0f else 10f
        val damage = if (mod == SpellMod.PWR) 3f else 1f
        val h = ManaHandle(user)
        val stk = user.getStackInHand(hand)
        val rank = mkRank(stk.orCreateNbt.getInt("tier"))
        if (!rank.canCast(WandRank.TIER1)) {
            if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
            return@run SpellCastResult.FAIL
        }
        if (h.mana >= cost) {
        } else return@run SpellCastResult.FAIL
        val bolt = ImmolateProj(ModEntities.ImmolateType, user, world)
        bolt.setNoGravity(true)
        bolt.setPosition(user.eyePos)
        bolt.damage = damage
        bolt.lifespan = reach
        bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 1F, divergence)
        world.spawnEntity(bolt)
        h.mana -= cost
        h.syncMana()
        return@run SpellCastResult.SUCCESS
    }}
}