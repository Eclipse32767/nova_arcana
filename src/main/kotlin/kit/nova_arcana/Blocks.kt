package kit.nova_arcana

import kit.nova_arcana.blocks.Pedestal
import kit.nova_arcana.blocks.RitualBlock
import kit.nova_arcana.blocks.StaffWorkbench
import kit.nova_arcana.blocks.SupportBlk
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
object ModBlocks {
    val STAFF_WORKBENCH = register("staff-workbench", StaffWorkbench(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque()))
    val STAFF_WORKBENCH_ITEM = Registry.register(Registries.ITEM, "nova_arcana:staff-workbench", BlockItem(STAFF_WORKBENCH, FabricItemSettings()))
    val SUPPORT_BLOCK = register("support-spellblock", SupportBlk(FabricBlockSettings.copyOf(Blocks.FROSTED_ICE).nonOpaque()))
    val RITUALBLOCK = register("rit-block", RitualBlock(FabricBlockSettings.create()))
    val RITUALBLOCK_ITEM = Registry.register(Registries.ITEM, "nova_arcana:rit-block", BlockItem(RITUALBLOCK, FabricItemSettings()))
    val PEDESTAL = register("pedestal", Pedestal(FabricBlockSettings.create().nonOpaque()))
    val PEDESTAL_ITEM = Registry.register(Registries.ITEM, "nova_arcana:pedestal", BlockItem(PEDESTAL, FabricItemSettings()))
}