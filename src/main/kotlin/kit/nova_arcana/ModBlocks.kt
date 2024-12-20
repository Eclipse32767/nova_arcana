package kit.nova_arcana

import kit.nova_arcana.blocks.*
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

private fun<T: Block> register(path: String, item: T): T {
    return Registry.register(Registries.BLOCK, Identifier("nova_arcana", path), item);
}
private fun<T: Block> register(path: String, item: T, set: FabricItemSettings): T {
    val block = register(path, item)
    val item = Registry.register(Registries.ITEM, Identifier("nova_arcana", path), BlockItem(block, set))
    return block
}
object ModBlocks {
    val STAFF_WORKBENCH = register("staff-workbench", StaffWorkbench(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque()), FabricItemSettings())
    val SUPPORT_BLOCK = register("support-spellblock", SupportBlk(FabricBlockSettings.copyOf(Blocks.FROSTED_ICE).nonOpaque()), FabricItemSettings())
    val RITUALBLOCK = register("rit-block", RitualBlock(FabricBlockSettings.create()), FabricItemSettings())
    val PEDESTAL = register("pedestal", Pedestal(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val DECONSTRUCTOR = register("deconstructor", Deconstructor(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val INFUSION_STONE = register("infusion-stone", InfusionStone(FabricBlockSettings.copyOf(Blocks.GLASS)), FabricItemSettings())
    val MANA_VESSEL_FIRE = register("mana-vessel-fire", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.FIRE), FabricItemSettings())
    val MANA_VESSEL_ICE = register("mana-vessel-ice", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.ICE), FabricItemSettings())
    val MANA_VESSEL_EARTH = register("mana-vessel-earth", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.EARTH), FabricItemSettings())
    val MANA_VESSEL_WIND = register("mana-vessel-wind", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.WIND), FabricItemSettings())
    val MANA_VESSEL_SPIRIT = register("mana-vessel-spirit", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.SPIRIT), FabricItemSettings())
    val MANA_VESSEL_VOID = register("mana-vessel-void", ManaVessel(FabricBlockSettings.copyOf(Blocks.GLASS), ManaFilter.VOID), FabricItemSettings())
    val RITUAL_SOARING = register("ritual-soaring", RitualSoaringBlock(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val RITUAL_FORGE = register("ritual-forge", RitualForge(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val RITUAL_HOARD = register("ritual-hoard", RitualHoard(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val RITUAL_PRISON = register("ritual-prison", RitualImprisoned(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque()), FabricItemSettings())
    val BARRIER = register("magic-barrier", BarrierBlock(FabricBlockSettings.copyOf(Blocks.GLASS).strength(50f).hardness(50f)))
    val MANA_FIRE = register("mana-fire", Block(FabricBlockSettings.create()), FabricItemSettings())
}