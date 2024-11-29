package kit.nova_arcana

import kit.nova_arcana.effects.FairGroundHex
import kit.nova_arcana.effects.Overclocked
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


object ModEffects {
    val FAIR_GROUND = Registry.register(Registries.STATUS_EFFECT, Identifier("nova_arcana:fairground-hex"), FairGroundHex())
    val OVERCLOCKED = Registry.register(Registries.STATUS_EFFECT, Identifier("nova_arcana:overclocked"), Overclocked())
}