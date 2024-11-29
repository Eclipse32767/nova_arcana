package kit.nova_arcana.effects

import kit.nova_arcana.ModEffects
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Items

class Overclocked: StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFFFF) {
    override fun onRemoved(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onRemoved(entity, attributes, amplifier)
    }

    override fun onApplied(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        entity.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 100000, amplifier))
        entity.addStatusEffect(StatusEffectInstance(StatusEffects.HASTE, 100000, amplifier))
        entity.addStatusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 100000, amplifier))
        super.onApplied(entity, attributes, amplifier)
    }
}