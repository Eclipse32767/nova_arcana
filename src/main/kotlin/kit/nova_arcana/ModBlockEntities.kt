package kit.nova_arcana

import kit.nova_arcana.blocks.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder

private fun<T: BlockEntity> register(path: String, type: BlockEntityType<T>): BlockEntityType<T> {
    return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier("nova_arcana", path), type)
}

object ModBlockEntities {
    val RITUAL_TYPE = register("rit-block", FabricBlockEntityTypeBuilder.create({a, b -> RitualBlockEntity(a, b)}, ModBlocks.RITUALBLOCK).build(null))
    val PEDESTAL_TYPE = register("pedestal", FabricBlockEntityTypeBuilder.create({a, b -> PedestalEntity(a, b)}, ModBlocks.PEDESTAL).build(null))
    val INFUSION_STONE_TYPE = register("infusion-stone", FabricBlockEntityTypeBuilder.create({a, b -> InfusionStoneEntity(a, b)}, ModBlocks.INFUSION_STONE).build(null))
    val MANA_VESSEL_TYPE = register("mana-vessel", FabricBlockEntityTypeBuilder.create({a, b -> ManaVesselEntity(a, b, ManaFilter.ICE)},
        ModBlocks.MANA_VESSEL_ICE,
        ModBlocks.MANA_VESSEL_FIRE,
        ModBlocks.MANA_VESSEL_EARTH,
        ModBlocks.MANA_VESSEL_WIND,
        ModBlocks.MANA_VESSEL_SPIRIT,
        ModBlocks.MANA_VESSEL_VOID).build(null))
    val RIT_SOARING_TYPE = register("ritual-soaring", FabricBlockEntityTypeBuilder.create({a, b -> RitualSoaringBlockEntity(a, b)}, ModBlocks.RITUAL_SOARING).build())
    val RIT_FORGE_TYPE = register("ritual-forge", FabricBlockEntityTypeBuilder.create({a, b -> RitualForgeBlockEntity(a, b)}, ModBlocks.RITUAL_FORGE).build())
    val RIT_HOARD_TYPE = register("ritual-hoard", FabricBlockEntityTypeBuilder.create({a, b -> RitualHoardBlockEntity(a, b)}, ModBlocks.RITUAL_HOARD).build())
    val RIT_PRISON_TYPE = register("ritual-prison", FabricBlockEntityTypeBuilder.create({a, b -> RitualImprisonedBlockEntity(a, b)}, ModBlocks.RITUAL_PRISON).build())
    val BARRIER_TYPE = register("magic-barrier", FabricBlockEntityTypeBuilder.create({a, b -> BarrierEntity(a, b)}, ModBlocks.BARRIER).build())
}