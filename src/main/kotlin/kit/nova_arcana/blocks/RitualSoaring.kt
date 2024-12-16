package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModEffects
import kit.nova_arcana.ModEntities
import kit.nova_arcana.entities.ManaBeam
import kit.nova_arcana.recipes.ManaOutputs
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

private val ACTIVE = BooleanProperty.of("active")

class RitualSoaringBlock(settings: FabricBlockSettings) : BlockWithEntity(settings.luminance { if (it.get(ACTIVE)) 2 else 0 }), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RitualSoaringBlockEntity(pos, state)
    }
    val h = run {
        defaultState = defaultState.with(ACTIVE, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(ACTIVE)
    }
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is RitualSoaringBlockEntity) entity.tick(world, pos, state)}
    }
}

class RitualSoaringBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(ModBlockEntities.RIT_SOARING_TYPE, pos, state) {
    var tickcount = 0
    val manacost = ManaOutputs(0, 0, 0, 2, 2, 0)
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        tickcount++
        if (tickcount % 20 != 0) return
        if (world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.with(ACTIVE, false))
            return
        }
        val players = world.getOtherEntities(null, Box.of(pos.toCenterPos(), 20.0, 80.0, 20.0)).filterIsInstance<PlayerEntity>()
        if (players.isEmpty()) {
            world.setBlockState(pos, state.with(ACTIVE, false))
            return
        }
        val manaPool = mutableListOf<ManaVesselEntity>()
        for (x in -10..10) for (y in -3..3) for (z in -10..10) {
            val vessel = world.getBlockEntity(BlockPos(pos.x + x, pos.y + y, pos.z + z))
            if (vessel is ManaVesselEntity) manaPool += vessel
        }
        //manaPool.shuffle()
        val manaNeeds = manacost.pairList()
        for (need in manaNeeds) {
            var amtMet = need.second
            for (vessel in manaPool) {
                val subtracted = vessel.sub(need.first, amtMet)
                vessel.markDirty()
                amtMet -= subtracted
                if (subtracted <= 0) continue
                val line = ManaBeam(ModEntities.ManaBeamType, world)
                line.color1 = need.first.a
                line.color2 = need.first.b
                line.startScale = 0.20f
                line.dest = pos.toCenterPos()
                line.setPosition(vessel.pos.toCenterPos())
                line.setNoGravity(true)
                //line.mvTowardTrgt()
                world.spawnEntity(line)
            }
            if (amtMet > 0) {
                world.setBlockState(pos, state.with(ACTIVE, false))
                return
            }
        }
        for (e in players) {
            val line = ManaBeam(ModEntities.ManaBeamType, world)
            line.color1 = Color(100, 0, 100)
            line.color2 = Color(0, 100, 200)
            line.startScale = 0.20f
            line.dest = pos.toCenterPos()
            line.particleLifespan = 5
            line.lifespan = 5
            line.density = 2
            line.setPosition(e.pos)
            line.setNoGravity(true)
            //line.mvTowardTrgt()
            world.spawnEntity(line)
            val effect = e.getStatusEffect(ModEffects.SOARING)
            if (effect != null && effect.duration < 60) {
                effect.duration = 60
            }
            if (effect == null) {
                e.addStatusEffect(StatusEffectInstance(ModEffects.SOARING, 60, 0))
            }
        }
        world.setBlockState(pos, state.with(ACTIVE, true))
    }
}