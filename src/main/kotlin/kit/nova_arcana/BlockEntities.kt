package kit.nova_arcana

import kit.nova_arcana.blocks.RitualBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import kit.nova_arcana.blocks.InfusionStoneEntity
import kit.nova_arcana.blocks.PedestalEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder

private fun<T: BlockEntity> register(path: String, type: BlockEntityType<T>): BlockEntityType<T> {
    return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier("nova_arcana", path), type)
}

object ModBlockEntities {
    val RITUAL_TYPE = register("rit-block", FabricBlockEntityTypeBuilder.create({a, b -> RitualBlockEntity(a, b)}, ModBlocks.RITUALBLOCK).build(null))
    val PEDESTAL_TYPE = register("pedestal", FabricBlockEntityTypeBuilder.create({a, b -> PedestalEntity(a, b)}, ModBlocks.PEDESTAL).build(null))
    val INFUSION_STONE_TYPE = register("infusion-stone", FabricBlockEntityTypeBuilder.create({a, b -> InfusionStoneEntity(a, b)}, ModBlocks.INFUSION_STONE).build(null))
}