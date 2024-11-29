package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.entities.ExcavateItem
import kit.nova_arcana.entities.PlacementWisp
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger
import team.lodestar.lodestone.systems.client.ClientTickCounter

fun regSubstitute(logger: Logger) {
    registerSpell(Identifier("nova_arcana:spell/substitute"), Text.literal("Substitute"), Identifier("nova_arcana:item/mat-blank")) { world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 10 else if (mod == SpellMod.PWR) 100 else 20
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val count = if (mod == SpellMod.PWR) 1 else 0
        val h = ManaHandle(user)
        val stk = user.getStackInHand(hand)
        val rank = mkRank(stk.orCreateNbt.getInt("tier"))
        if (!rank.canCast(WandRank.TIER1)) {
            if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
            return@run SpellCastResult.FAIL
        }
        if (h.mana >= cost) {
        } else return@run SpellCastResult.FAIL
        val cast = user.raycast(reach, 0.0f, true)
        if (cast.type == HitResult.Type.BLOCK) {
            if (user.mainHandStack.item == ModItems.wand && user.offHandStack.item is BlockItem) {
            } else return@run SpellCastResult.FAIL
            val replacement = user.offHandStack.item as BlockItem
            val blockHit = cast as BlockHitResult
            val perpendicular = when (blockHit.side.axis) {
                Direction.Axis.X -> Pair(Direction.Axis.Y, Direction.Axis.Z)
                Direction.Axis.Y -> Pair(Direction.Axis.X, Direction.Axis.Z)
                Direction.Axis.Z -> Pair(Direction.Axis.X, Direction.Axis.Y)
            }
            val positions: MutableList<BlockPos> = mutableListOf()
            for (i in (-count..count)) {
                for (j in (-count..count)) {
                    var pos = blockHit.blockPos.offset(perpendicular.first, i)
                    pos = pos.offset(perpendicular.second, j)
                    positions += pos
                }
            }
            positions.distinct()
            for (blockPos in positions) {
                val hardness = world.getBlockState(blockPos).block.hardness
                if (hardness > 0 && hardness < 9) {
                    if (world.isClient) {
                        val spawner = excavateParticle(0.25f, 0.0f)
                        for (i in (0..5)) {
                            spawner.createBlockOutline(world, blockPos, world.getBlockState(blockPos))
                        }
                        continue
                    }
                    if (user.offHandStack.isEmpty) break
                    user.offHandStack.decrement(1)
                    val drops = world.getBlockState(blockPos).getDroppedStacks(
                        LootContextParameterSet.Builder(world as ServerWorld?)
                            .add(LootContextParameters.ORIGIN, Vec3d(0.0, 0.0, 0.0))
                            .add(LootContextParameters.TOOL, Items.DIAMOND_PICKAXE.defaultStack)
                    )
                    for (drop in drops) {
                        val itm = ExcavateItem(ModEntities.ExcavateItemType, user, world)
                        itm.setPosition(blockPos.toCenterPos())
                        itm.setStk(drop)
                        itm.setNoGravity(true)
                        world.spawnEntity(itm)
                    }
                    world.breakBlock(blockPos, false)
                    //logger.atInfo().log(blockPos.toString())

                    val bolt = PlacementWisp(ModEntities.PlacementType, world, blockPos, replacement.block.defaultState)
                    bolt.setPosition(user.eyePos)
                    bolt.mvTowardTrgt()
                    bolt.setNoGravity(true)
                    world.spawnEntity(bolt)
                    //world.setBlockState(blockPos, replacement.block.defaultState)
                } else continue
            }
            h.mana -= cost
            h.syncMana()
        }
        return@run SpellCastResult.SUCCESS
    }}
    spellInvReg[Identifier("nova_arcana:spell/substitute")] = SpellInv { stk, world, entity, slot, selected, mod -> run {
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val count = if (mod == SpellMod.PWR) 1 else 0
        val cast = entity.raycast(reach, 0.0f, true)
        if (!world.isClient) return@run
        if (cast.type == HitResult.Type.BLOCK) {
            val blockHit = cast as BlockHitResult
            val perpendicular = when (blockHit.side.axis) {
                Direction.Axis.X -> Pair(Direction.Axis.Y, Direction.Axis.Z)
                Direction.Axis.Y -> Pair(Direction.Axis.X, Direction.Axis.Z)
                Direction.Axis.Z -> Pair(Direction.Axis.X, Direction.Axis.Y)
            }
            val positions: MutableList<BlockPos> = mutableListOf()
            for (i in (-count..count)) {
                for (j in (-count..count)) {
                    var pos = blockHit.blockPos.offset(perpendicular.first, i)
                    pos = pos.offset(perpendicular.second, j)
                    positions += pos
                }
            }
            positions.distinct()
            for (pos in positions) {
                //excavateParticle(0.10f, 0.0f).createBlockOutline(world, pos, world.getBlockState(pos))
                if (ClientTickCounter.ticksInGame % 4 == 0L) supportParticle(0.10f, 0.0f).createBlockOutline(world, pos, world.getBlockState(pos))
            }
        }
    }}
}