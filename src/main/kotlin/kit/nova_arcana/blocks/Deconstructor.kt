package kit.nova_arcana.blocks

import kit.nova_arcana.ModEntities
import kit.nova_arcana.Recipes
import kit.nova_arcana.entities.InfusionParticleLine
import kit.nova_arcana.entities.ManaBeam
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.DispenserBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.RedstoneView
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

private val TRIGGERED = BooleanProperty.of("triggered")

class Deconstructor(settings: FabricBlockSettings): Block(settings) {
    val logger = LoggerFactory.getLogger("deconstructor_log")
    val h = run {
        defaultState = defaultState.with(TRIGGERED, false)
    }
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(TRIGGERED)
    }
    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        sourcePos: BlockPos,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
        if (world.isClient) return
        val view = world as RedstoneView
        val bl = view.isReceivingRedstonePower(pos) || view.isReceivingRedstonePower(pos.up())
        val bl2 = state.get(TRIGGERED) as Boolean
        if (bl && !bl2) {
            world.scheduleBlockTick(pos, this, 4)
            world.setBlockState(pos, state.with(TRIGGERED, true), NO_REDRAW)
        } else if (!bl && bl2) {
            world.setBlockState(pos, state.with(TRIGGERED, false), NO_REDRAW)
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.scheduledTick(state, world, pos, random)
        val pedestalPos = BlockPos(pos.x, pos.y+1, pos.z)
        val pedestal = world.getBlockEntity(pedestalPos)
        //logger.atInfo().log("am powered")
        if (pedestal !is PedestalEntity) return
        val recipe = world.recipeManager.getFirstMatch(Recipes.DECONSTRUCTION, pedestal, world).getOrNull()
            ?: //logger.atInfo().log("recipe is null")
            return
        //logger.atInfo().log("pedestal above holding ${pedestal.stack.item}")
        var successful = 0
        for (pair in recipe.output.pairList()) {
            //logger.atInfo().log("distributing ${pair.second} ${pair.first} mana")
            var v = pair.second + 0
            for (x in -5..5) for (y in -3..3) for (z in -5..5) {
                if (v == 0) continue
                val entity = world.getBlockEntity(BlockPos(pos.x + x, pos.y + y, pos.z + z))
                if (entity is ManaVesselEntity) {
                    val old = v
                    v = entity.add(pair.first, v)
                    if (old != v) {
                        val bolt = ManaBeam(ModEntities.ManaBeamType, world)
                        bolt.setPosition(pos.toCenterPos().add(0.0, 1.5, 0.0))
                        //bolt.mvTowardTrgt()
                        bolt.dest = entity.pos.toCenterPos()
                        bolt.setNoGravity(true)
                        bolt.color1 = pair.first.a
                        bolt.color2 = pair.first.b
                        bolt.startScale = 0.20f
                        world.spawnEntity(bolt)
                        successful++
                        //logger.atInfo().log("spawning wisp")
                    }
                    entity.markDirty()
                }
            }
        }
        if (successful != 0) {
            pedestal.inv.decrement(1)
            pedestal.markDirty()
        }
    }
}