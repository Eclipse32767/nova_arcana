package kit.nova_arcana

import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkMateria
import kit.nova_arcana.items.mkModMateria
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import net.minecraft.text.Text

object Prefabs {
    val CORE_OAK = corePartStk("nova_arcana:item/wand-core-basic", "Oak Staff Core", 2, 0)
    val DECOR_CLAW_BASIC = decorPartStk("nova_arcana:item/wand-claw-basic", "Staff Decor: Basic Claw")
    val DECOR_ORB_BASIC = decorPartStk("nova_arcana:item/wand-orb-basic", "Staff Decor: Basic Orb")
    val GEM_EMERALD = gemPartStk("nova_arcana:item/wand-gem-emerald", "Emerald", WandRank.EARTHONLY)
    val GEM_RUBY = gemPartStk("nova_arcana:item/wand-gem-emerald", "Ruby", WandRank.FIREONLY)
    val GEM_PRISMARINE = gemPartStk("nova_arcana:item/wand-gem-emerald", "Prismarine", WandRank.FROSTONLY)
    val GEM_AMBER = gemPartStk("nova_arcana:item/wand-gem-emerald", "Amber", WandRank.AIRONLY)
    val GEM_QUARTZ = gemPartStk("nova_arcana:item/wand-gem-emerald", "Quartz", WandRank.SPIRITONLY)
    val GEM_AMETHYST = gemPartStk("nova_arcana:item/wand-gem-emerald", "Amethyst", WandRank.VOIDONLY)
}


private fun itemWithNBT(itm: Item, tag: String, v: Int): ItemStack {
    val stk = itm.defaultStack
    stk.orCreateNbt.putInt(tag, v)
    return stk
}
private fun wandStk(core: String, decor: String, gem: String, name: String, s: Int, f: Int, tier: WandRank, vararg spells: String): ItemStack {
    val wand = ModItems.wand.defaultStack
    val nbt = wand.orCreateNbt
    nbt.putInt("tier", tier.num)
    nbt.putString("core", core)
    nbt.putString("decor", decor)
    nbt.putString("gem", gem)
    nbt.putString("name", name)
    nbt.putInt("slots_s", s)
    nbt.putInt("slots_f", f)
    nbt.putString("avail_spells", spells.joinToString('\n'.toString()))
    return wand
}
private fun wandStk(core: String, decor: String, gem: String, vararg spells: String): ItemStack {
    val wand = ModItems.wand.defaultStack
    val nbt = wand.orCreateNbt
    nbt.putInt("tier", -1)
    nbt.putString("core", core)
    nbt.putString("decor", decor)
    nbt.putString("gem", gem)
    nbt.putString("avail_spells", spells.joinToString('\n'.toString()))
    return wand
}
fun corePartStk(model: String, name: String, s: Int, f: Int): ItemStack {
    val core = ModItems.wandCore.defaultStack
    val nbt = core.orCreateNbt
    nbt.putString("name", name)
    nbt.putString("model", model)
    nbt.putInt("slots_s", s)
    nbt.putInt("slots_f", f)
    return core
}
fun decorPartStk(model: String, name: String): ItemStack {
    val decor = ModItems.wandDecor.defaultStack
    val nbt = decor.orCreateNbt
    nbt.putString("name", name)
    nbt.putString("model", model)
    return decor
}
fun gemPartStk(model: String, name: String, rank: WandRank): ItemStack {
    val gem = ModItems.wandGem.defaultStack
    val nbt = gem.orCreateNbt
    nbt.putString("name", name)
    nbt.putString("model", model)
    nbt.putInt("rank", rank.num)
    return gem
}
private fun blockStk(a: Block): ItemStack {
    val stk = a.asItem().defaultStack
    stk.count = 1
    return stk
}
fun matStk(a: String): ItemStack {
    return mkMateria("nova_arcana:spell/$a")
}


fun mkMainGrp() {
    val wandkey = RegistryKey.of(Registries.ITEM_GROUP.key, Identifier.of("nova_arcana", "wands"));
    val grpWands = FabricItemGroup.builder().icon { wandStk("nova_arcana:item/wand-core-basic", "nova_arcana:item/wand-claw-basic", "nova_arcana:item/wand-gem-emerald") }
        .displayName(Text.translatable("itemGroup.nova_arcana.wands")).entries { _, entries -> run {
            val decors = listOf("nova_arcana:item/wand-claw-basic", "nova_arcana:item/wand-orb-basic")
            /*
            decors.map { decor -> run {
                entries.add(wandStk("nova_arcana:item/wand-core-basic", decor, "nova_arcana:item/wand-gem-emerald", "Emerald Staff", 1, 0, WandRank.EARTHONLY, "nova_arcana:spell/excavate"))
            }}
             */
            entries.add(Prefabs.CORE_OAK)
            entries.add(Prefabs.DECOR_CLAW_BASIC)
            entries.add(Prefabs.DECOR_ORB_BASIC)
            entries.add(Prefabs.GEM_EMERALD)
            entries.add(Prefabs.GEM_RUBY)
            entries.add(Prefabs.GEM_PRISMARINE)
            entries.add(Prefabs.GEM_AMBER)
            entries.add(Prefabs.GEM_AMETHYST)
            entries.add(Prefabs.GEM_QUARTZ)
            entries.add(corePartStk("nova_arcana:item/wand-core-basic", "Dirty Cheater's Staff Core", 1, 13))
            entries.add(gemPartStk("nova_arcana:item/wand-gem-emerald", "CheaterGem", WandRank.CHEATER))
    }}.build()
    val blkkey = RegistryKey.of(Registries.ITEM_GROUP.key, Identifier.of("nova_arcana", "blocks"))
    val grpBlks = FabricItemGroup.builder().icon {ModBlocks.STAFF_WORKBENCH.asItem().defaultStack}
        .displayName(Text.translatable("itemGroup.nova_arcana.blocks")).entries {_, entries -> run {
            entries.add(ItemStack(ModBlocks.STAFF_WORKBENCH, 1))
        }}.build()
    val matKey = RegistryKey.of(Registries.ITEM_GROUP.key, Identifier.of("nova_arcana", "spells"))
    val grpMateria = FabricItemGroup.builder().icon { mkMateria("nova_arcana:spell/flame") }
        .displayName(Text.translatable("itemGroup.nova_arcana.spells")).entries {_, entries -> run {
            entries.add(matStk("flame"))
            entries.add(matStk("siphon"))
            entries.add(matStk("excavate"))
            entries.add(matStk("support"))
            entries.add(matStk("dash"))
            entries.add(matStk("recovery"))
            entries.add(matStk("substitute"))
            entries.add(mkModMateria(SpellMod.EFF, "nova_arcana:item/mat-eff"))
            entries.add(mkModMateria(SpellMod.PWR, "nova_arcana:item/mat-pwr"))
            entries.add(mkModMateria(SpellMod.AREA, "nova_arcana:item/mat-area"))
        }}.build()
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { run {
        it.add(ModItems.alchRuby)
        it.add(ModItems.amber)
    } }
    Registry.register(Registries.ITEM_GROUP, wandkey, grpWands)
    Registry.register(Registries.ITEM_GROUP, blkkey, grpBlks)
    Registry.register(Registries.ITEM_GROUP, matKey, grpMateria)
}