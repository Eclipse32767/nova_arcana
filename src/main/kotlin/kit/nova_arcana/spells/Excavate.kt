package kit.nova_arcana.spells

import kit.nova_arcana.*
import kit.nova_arcana.entities.ExcavateItem
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.item.Items
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger

fun regExcavate(logger: Logger) {
    registerSpell(
        Identifier("nova_arcana:spell/excavate"), Text.literal("Excavate"), Identifier("nova_arcana:item/mat-excavate")
    ) { world, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
            val maxHardness = if (mod == SpellMod.PWR) 40 else 9
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.EARTHONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }

            val hit = user.raycast(reach, 0.0F, false)
            if (h.mana >= cost) {
            } else return@run SpellCastResult.FAIL
            if (hit.type == HitResult.Type.BLOCK) {
                val blockHit = hit as BlockHitResult
                val blockPos = blockHit.blockPos
                val hardness = world.getBlockState(blockPos).block.hardness
                if (hardness > 0 && hardness < maxHardness) {
                    if (!world.isClient) {
                        val packet = excavateParticlePacket(0.25f, blockPos, false, 5)
                        for (plr in PlayerLookup.tracking(world as ServerWorld, blockPos).filter { it != user }) {
                            packet.sendTo(plr)
                        }
                        packet.sendTo(user as ServerPlayerEntity)
                    } else return@run SpellCastResult.SUCCESS

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
                    h.mana -= cost
                } else return@run SpellCastResult.FAIL
            } else return@run SpellCastResult.FAIL
            h.syncMana()
            SpellCastResult.SUCCESS
        }
    }
    spellInvReg[Identifier("nova_arcana:spell/excavate")] = SpellInv { stk, world, entity, slot, selected, mod -> run {
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val cast = entity.raycast(reach, 0.0f, false)
        if (cast.type != HitResult.Type.BLOCK) return@run
        val hit = cast as BlockHitResult
        if (!world.isClient) {
            val packet = excavateParticlePacket(0.10f, hit.blockPos, false, 0)
            for (plr in PlayerLookup.tracking(world as ServerWorld, hit.blockPos).filter { it != entity }) {
                packet.sendTo(plr)
            }
            if (entity is ServerPlayerEntity) packet.sendTo(entity)
        }
    }}
}