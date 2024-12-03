package kit.nova_arcana.effects

import kit.nova_arcana.ModEffects
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import org.slf4j.LoggerFactory

class SoaringBlessing: StatusEffect(StatusEffectCategory.NEUTRAL, 0xFFFFFF) {
    val logger = LoggerFactory.getLogger("soaring")
    override fun canApplyUpdateEffect(duration: Int, amplifier: Int): Boolean {
        return true
    }

    override fun onApplied(entity: LivingEntity?, attributes: AttributeContainer?, amplifier: Int) {
        super.onApplied(entity, attributes, amplifier)
        if (entity is PlayerEntity) {
            entity.abilities.allowFlying = true
            //entity.abilities.flying = true
            entity.sendAbilitiesUpdate()
        }
    }
    override fun applyUpdateEffect(entity: LivingEntity, amplifier: Int) {
        super.applyUpdateEffect(entity, amplifier)
        if (entity.world.isClient) return
        if (entity is PlayerEntity) {
            //val timeSinceHit = entity.world.time - entity.lastAttackedTime
            //logger.atInfo().log("${timeSinceHit}, ${entity.abilities.allowFlying}, ${entity.abilities.flying}")
            if (entity.recentDamageSource != null) {
                entity.abilities.allowFlying = false
                entity.abilities.flying = false
                entity.sendAbilitiesUpdate()
            } else if (!entity.abilities.allowFlying) {
                entity.abilities.allowFlying = true
                //entity.abilities.flying = true
                entity.sendAbilitiesUpdate()
            }
        }
    }

    override fun onRemoved(entity: LivingEntity, attributes: AttributeContainer, amplifier: Int) {
        super.onRemoved(entity, attributes, amplifier)
        if (entity is PlayerEntity && !entity.isCreative && !entity.isSpectator) {
            entity.abilities.allowFlying = false
            entity.abilities.flying = false
            entity.sendAbilitiesUpdate()
        }
    }
}