package kit.nova_arcana.blocks

import kit.nova_arcana.ImplementedInventory
import kit.nova_arcana.ModBlockEntities
import kit.nova_arcana.ModEntities
import kit.nova_arcana.Recipes
import kit.nova_arcana.entities.InfusionParticleLine
import kit.nova_arcana.recipes.InfusionRecipe
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap

class InfusionStone(settings: FabricBlockSettings): BlockWithEntity(settings), BlockEntityProvider {
    val logger = LoggerFactory.getLogger("infusion_stone")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val entity = world.getBlockEntity(pos)
        if (entity is InfusionStoneEntity) entity.checksLeft = 600
        return ActionResult.SUCCESS
    }


    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return InfusionStoneEntity(pos, state)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity?> getTicker(
        wld: World,
        st: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker {world, pos, state, entity -> run {
            if (entity is InfusionStoneEntity)  entity.tick(world, pos, state)
        }}
    }
}

class InfusionStoneEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.INFUSION_STONE_TYPE, pos, state) {
    class RecipeHandle(val central: Ingredient, val inputs: MutableList<Ingredient>, val output: ItemStack)
    fun handleFrom(r: InfusionRecipe): RecipeHandle {
        return RecipeHandle(r.central, r.inputs.toMutableList(), r.output.copy())
    }
    var checksLeft = 0
    var targetRecipe: RecipeHandle? = null
    var tickCount = 0
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        val logger = LoggerFactory.getLogger("pedestal_crafting")
        if (world.isClient) return
        checksLeft--

        val central = world.getBlockEntity(BlockPos(pos.x, pos.y - 2, pos.z)) ?: return
        val reagents = mutableListOf<PedestalEntity>()
        for (x in -5..5) {
            for (z in -5..5) {
                val pedestal = world.getBlockEntity(BlockPos(pos.x + x, pos.y - 3, pos.z + z))
                if (pedestal is PedestalEntity) {
                    if (!pedestal.stack.isEmpty) reagents += pedestal
                }
            }
        }
        if (central !is PedestalEntity) return
        if (checksLeft > 0 && targetRecipe == null) {
            //logger.atInfo().log("$tickCount: $checksLeft checks remaining")
            val recipe = getRecipe(world, reagents, central)
            recipe.ifPresent {
                //logger.atInfo().log("got the recipe boss")
                targetRecipe = handleFrom(it)
            }
        }
        tickCount++
        if (central.inv.isEmpty) {
            targetRecipe = null
            return
        }
        if (targetRecipe != null && tickCount % 20 == 0) {
            val targetRecipe = targetRecipe!!
            if (targetRecipe.inputs.isEmpty()) {
                central.inv.decrement(1)
                central.markDirty()
                if (central.inv.isEmpty) {
                    central.stack = targetRecipe.output
                    central.markDirty()
                } else {
                    val out = ItemEntity(EntityType.ITEM, world)
                    out.stack = targetRecipe.output
                    val newpos = BlockPos(pos.x, pos.y - 1, pos.z)
                    out.setPosition(newpos.toCenterPos())
                    world.spawnEntity(out)
                }
                this.targetRecipe = null
                checksLeft = 0
            } else {
                for (stk in reagents) {
                    if (targetRecipe.inputs[0].test(stk.inv)) {
                        consumePedestal(stk, world)
                        break
                    }
                }
            }
        }
    }
    fun consumePedestal(stk: PedestalEntity, world: World) {
        stk.inv.decrement(1)
        stk.markDirty()
        this.targetRecipe!!.inputs.removeAt(0)
        val line = InfusionParticleLine(ModEntities.InfusionParticleType, world, Color.CYAN, Color.BLUE, pos.toCenterPos().add(0.0, -1.0, 0.0))
        line.setPosition(stk.pos.toCenterPos().add(0.0, 1.0, 0.0))
        line.setNoGravity(true)
        line.mvTowardTrgt()
        world.spawnEntity(line)
    }
    fun getRecipe(world: World, reagents: List<PedestalEntity>, central: PedestalEntity): Optional<InfusionRecipe> {
        return world.recipeManager.getFirstMatch(Recipes.INFUSION, InfusionPedestals(central, reagents), world)
    }

    override fun deserializeNBT(nbt: NbtCompound) {
        checksLeft = 0
        tickCount = 0
        targetRecipe = null
    }
}


class InfusionPedestals(val central: PedestalEntity, val reagents: List<PedestalEntity>): ImplementedInventory {
    override fun getItems(): DefaultedList<ItemStack> {
        //val logger = LoggerFactory.getLogger("pedestal_crafting")
        val list = DefaultedList.ofSize(reagents.size, ItemStack.EMPTY)
        for (i in reagents.indices) {
            list[i] = reagents[i].stack
            //logger.atInfo().log("ingredients: ${reagents[i].stack.item}")
        }
        return list
    }
}