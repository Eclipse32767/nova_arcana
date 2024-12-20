package kit.nova_arcana.datagen

import kit.nova_arcana.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.data.DataOutput

class BlockLootGen(dataOutput: DataOutput) : FabricBlockLootTableProvider(dataOutput as FabricDataOutput?) {
    override fun generate() {
        addDrop(ModBlocks.DECONSTRUCTOR)
        addDrop(ModBlocks.STAFF_WORKBENCH)
        addDrop(ModBlocks.INFUSION_STONE)
        addDrop(ModBlocks.PEDESTAL)
        addDrop(ModBlocks.MANA_VESSEL_ICE)
        addDrop(ModBlocks.MANA_VESSEL_FIRE)
        addDrop(ModBlocks.MANA_VESSEL_EARTH)
        addDrop(ModBlocks.MANA_VESSEL_WIND)
        addDrop(ModBlocks.MANA_VESSEL_SPIRIT)
        addDrop(ModBlocks.MANA_VESSEL_VOID)
        addDrop(ModBlocks.RITUAL_FORGE)
        addDrop(ModBlocks.RITUAL_HOARD)
        addDrop(ModBlocks.RITUAL_PRISON)
        addDrop(ModBlocks.RITUAL_SOARING)
    }
}