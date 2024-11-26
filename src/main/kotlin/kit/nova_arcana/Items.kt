package kit.nova_arcana

import kit.nova_arcana.items.*
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

private fun<T: Item> register(path: String, item: T): T {
    return Registry.register(Registries.ITEM, Identifier("nova_arcana", path), item);
}

object ModItems {
    val wand = register("wand", WandItem(FabricItemSettings().maxCount(1)));
    val materia = register("materia", MateriaItem(FabricItemSettings().maxCount(1)))
    val wandCore = register("wand-core", WandCore(FabricItemSettings().maxCount(1)))
    val wandDecor = register("wand-decor", WandDecor(FabricItemSettings().maxCount(1)))
    val wandGem = register("wand-gem", WandGem(FabricItemSettings().maxCount(1)))
    val modMateria = register("mod-materia", ModMateria(FabricItemSettings().maxCount(1)))
    val alchRuby = register("ruby", Item(FabricItemSettings()))
    val amber = register("amber", Item(FabricItemSettings()))
    val MatSurrogates = object  {
        val effMat = register("mat-eff", SurrogateItem(mkModMateria(SpellMod.EFF, "")))
        val pwrMat = register("mat-pwr", SurrogateItem(mkModMateria(SpellMod.PWR, "")))
        val areaMat = register("mat-area", SurrogateItem(mkModMateria(SpellMod.AREA, "")))
        val flameMat = register("mat-flame", SurrogateItem(matStk("flame")))
        val siphonMat = register("mat-siphon", SurrogateItem(matStk("siphon")))
        val excavateMat = register("mat-excavate", SurrogateItem(matStk("excavate")))
        val supportMat = register("mat-support", SurrogateItem(matStk("support")))
        val dashMat = register("mat-dash", SurrogateItem(matStk("dash")))
        val recoveryMat = register("mat-recovery", SurrogateItem(matStk("recovery")))
    }
    val CoreSurrogates = object  {
        val coreBasic = register("wand-core-basic", SurrogateItem(Prefabs.CORE_OAK))
    }
    val DecorSurrogates = object {
        val decorClaw = register("wand-claw-basic", SurrogateItem(Prefabs.DECOR_CLAW_BASIC))
        val decorOrb = register("wand-orb-basic", SurrogateItem(Prefabs.DECOR_ORB_BASIC))
    }
    val GemSurrogates = object {
        val gemEmerald = register("wand-gem-emerald", SurrogateItem(Prefabs.GEM_EMERALD))
        val gemRuby = register("wand-gem-ruby", SurrogateItem(Prefabs.GEM_RUBY))
        val gemPrismarine = register("wand-gem-prismarine", SurrogateItem(Prefabs.GEM_PRISMARINE))
        val gemAmber = register("wand-gem-amber", SurrogateItem(Prefabs.GEM_AMBER))
        val gemAmethyst = register("wand-gem-amethyst", SurrogateItem(Prefabs.GEM_AMETHYST))
        val gemQuartz = register("wand-gem-quartz", SurrogateItem(Prefabs.GEM_QUARTZ))
    }
}
