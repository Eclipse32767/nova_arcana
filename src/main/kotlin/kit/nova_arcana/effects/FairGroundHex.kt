package kit.nova_arcana.effects

import kit.nova_arcana.ModEffects
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

class FairGroundHex() : StatusEffect(StatusEffectCategory.NEUTRAL, 0xFFFFFF) {
    override fun canApplyUpdateEffect(duration: Int, amplifier: Int): Boolean {
        return true
    }

    override fun applyUpdateEffect(entity: LivingEntity, amplifier: Int) {
        val effects = entity.statusEffects
        if (effects.filter { it.effectType == ModEffects.OVERCLOCKED }.count() == 1) {
            super.applyUpdateEffect(entity, amplifier)
            return
        }
        for (status in effects) {
            if (status.effectType == ModEffects.FAIR_GROUND) continue
            entity.removeStatusEffect(status.effectType)
        }
        super.applyUpdateEffect(entity, amplifier)
    }
}