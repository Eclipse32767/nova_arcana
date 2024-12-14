package kit.nova_arcana.blocks

import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModBlocks
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

private val ACTIVE = BooleanProperty.of("active")

class BarrierBlock(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    val h = run {
        defaultState = defaultState.with(ACTIVE, false)
    }
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(ACTIVE)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BarrierEntity(pos, state)
    }

    fun makeVis(state: BlockState, world: World, pos: BlockPos) {
        world.setBlockState(pos, state.with(ACTIVE, true))
    }
    fun makeInvis(state: BlockState, world: World, pos: BlockPos) {
        world.setBlockState(pos, state.with(ACTIVE, false))
    }
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun isTransparent(state: BlockState?, world: BlockView?, pos: BlockPos?) = true
    override fun isSideInvisible(state: BlockState?, stateFrom: BlockState, direction: Direction?): Boolean {
        return if (stateFrom.isOf(this) && stateFrom.get(ACTIVE)) true else super.isSideInvisible(state, stateFrom, direction)
    }
    override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?) = 1.0f
    override fun getOpacity(state: BlockState?, world: BlockView?, pos: BlockPos?) = 0
    override fun getCameraCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ) = VoxelShapes.empty()
    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> if (entity is BarrierEntity) entity.tick(world, pos, state)}
    }
}

class BarrierEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.BARRIER_TYPE, pos, state) {
    var rainflag = 0
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        val entities = world.getOtherEntities(null, Box.of(pos.toCenterPos(), 10.0, 10.0, 10.0)) {it.pos.distanceTo(pos.toCenterPos()) < 5 && it is LivingEntity}
        if (entities.isNotEmpty()) {
            ModBlocks.BARRIER.makeVis(state, world, pos)
        } else {
            ModBlocks.BARRIER.makeInvis(state, world, pos)
        }
    }
}