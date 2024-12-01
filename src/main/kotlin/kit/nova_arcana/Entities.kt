package kit.nova_arcana

import kit.nova_arcana.entities.*
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color

object ModEntities {
    val FireballProjType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:fire_bolt"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> FireballProj(type, world) }.dimensions(
            EntityDimensions(0.5F, 0.5F, true)
        ).trackRangeBlocks(10).trackedUpdateRate(10).build())
    val DrainBeamType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:drain_beam"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> DrainBeam(type, world) }.dimensions(
            EntityDimensions(0.5F, 0.5F, true)
        ).trackRangeBlocks(80).trackedUpdateRate(80).build())
    val SiphonHealType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:siphon_heal"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> SiphonHeal(type, world) }.dimensions(
            EntityDimensions(0.5F, 0.5F, true)
        ).trackRangeBlocks(10).trackedUpdateRate(10).build())
    val ExcavateItemType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:excavate_item"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> ExcavateItem(type, world) }.dimensions(
            EntityDimensions(0.5F, 0.5F, true)
        ).trackRangeBlocks(10).trackedUpdateRate(10).build())
    val PlacementType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:placement_wisp"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) {type, world -> PlacementWisp(type, world, BlockPos.ORIGIN, Blocks.AIR.defaultState) }
            .dimensions(
                EntityDimensions(0.5f, 0.5f, true)
            )
            .trackRangeBlocks(80).trackedUpdateRate(10).build())
    val ImmolateType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:immolate"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> ImmolateProj(type, world) }.dimensions(
            EntityDimensions(0.5F, 0.5F, true)
        ).trackRangeBlocks(80).trackedUpdateRate(80).build())
    val InfusionParticleType = Registry.register(Registries.ENTITY_TYPE, Identifier("nova_arcana:infusion_particle_spawner"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type, world -> InfusionParticleLine(type, world, Color.CYAN, Color.CYAN, Vec3d.ZERO)}.dimensions(
            EntityDimensions(0.5f, 0.5f, true)
        ).trackRangeBlocks(80).trackedUpdateRate(80).build()
    )
}