package kit.nova_arcana.spells

import kit.nova_arcana.ManaHandle
import kit.nova_arcana.SpellCastResult
import kit.nova_arcana.SpellMod
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
import org.slf4j.Logger
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

fun regMalevolence(logger: Logger) {
    registerSpell(Identifier("nova_arcana:spell/malevolence"), Text.literal("Malevolence"), Identifier("nova_arcana:item/mat-blank")) { world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 40 else 60
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
        val startCol = Color(100, 0, 100)
        val edCol = Color(0, 100, 200)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.5f, 0F).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        //spawner.spinData = SpinParticleData.create(0.2f, 0.4f).setSpinOffset((world.time * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build()
        spawner.setLifetime(40)
        spawner.setRandomMotion(0.0, 0.1, 0.0)
        spawner.enableNoClip()
        spawner.repeatCircle(world, user.x, user.y, user.z, area/2, 20)
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