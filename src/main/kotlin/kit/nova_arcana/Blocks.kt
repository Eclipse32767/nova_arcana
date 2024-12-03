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
    val PEDESTAL = register("pedestal", Pedestal(FabricBlockSettings.create().nonOpaque()), FabricItemSettings())
    val DECONSTRUCTOR = register("deconstructor", Deconstructor(FabricBlockSettings.create().nonOpaque()), FabricItemSettings())
    val INFUSION_STONE = register("infusion-stone", InfusionStone(FabricBlockSettings.create().nonOpaque()), FabricItemSettings())
    val MANA_VESSEL_FIRE = register("mana-vessel-fire", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.FIRE), FabricItemSettings())
    val MANA_VESSEL_ICE = register("mana-vessel-ice", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.ICE), FabricItemSettings())
    val MANA_VESSEL_EARTH = register("mana-vessel-earth", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.EARTH), FabricItemSettings())
    val MANA_VESSEL_WIND = register("mana-vessel-wind", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.WIND), FabricItemSettings())
    val MANA_VESSEL_SPIRIT = register("mana-vessel-spirit", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.SPIRIT), FabricItemSettings())
    val MANA_VESSEL_VOID = register("mana-vessel-void", ManaVessel(FabricBlockSettings.create().nonOpaque(), ManaFilter.VOID), FabricItemSettings())
    val RITUAL_SOARING = register("ritual-soaring", RitualSoaringBlock(FabricBlockSettings.create().nonOpaque()), FabricItemSettings())
}