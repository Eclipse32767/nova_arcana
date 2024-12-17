package kit.nova_arcana.spells

import kit.nova_arcana.ManaHandle
import kit.nova_arcana.SpellCastResult
import kit.nova_arcana.SpellMod
import kit.nova_arcana.fx.RingEffects
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import kit.nova_arcana.registerSpell
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

fun regMalevolence(logger: Logger) {
    registerSpell(Identifier("nova_arcana:spell/malevolence"), Text.literal("Malevolence"), Identifier("nova_arcana:item/mat-malevolence")) { world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 30 else 50
        val cap = if (mod == SpellMod.PWR) 20f else 10f
        val area = if (mod == SpellMod.AREA) 6.0 else 3.0
        val h = ManaHandle(user)
        val stk = user.getStackInHand(hand)
        val rank = mkRank(stk.orCreateNbt.getInt("tier"))
        if (!rank.canCast(WandRank.TIER1)) {
            if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
            return@run SpellCastResult.FAIL
        }
        if (h.mana >= cost) {
        } else return@run SpellCastResult.FAIL
        val targets = world.getOtherEntities(user, Box.of(user.pos, area, area, area))
        val damageval = minOf(user.maxHealth - user.health, cap) / targets.size
        //logger.atInfo().log(damageval.toString())
        if (world.isClient) {
            val fx = RingEffects(Color(100, 0, 100), Color(0, 100, 200), 40, 20, 0.5f, user.pos, area/2)
            fx.motion = Vec3d(0.0, 0.1, 0.0)
            fx.spawn(world)
        }
        for (target in targets) {
            if (target is LivingEntity) {
                val source = DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.MAGIC), user)
                target.damage(source, damageval)
            }
        }
        h.mana -= cost
        h.syncMana()
        return@run SpellCastResult.SUCCESS
    }}
}