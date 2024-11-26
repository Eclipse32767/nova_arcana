package kit.nova_arcana


import kit.nova_arcana.entities.DrainBeam
import kit.nova_arcana.entities.ExcavateItem
import kit.nova_arcana.entities.FireballProj
import kit.nova_arcana.entities.PlacementWisp
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import kit.nova_arcana.mixin.InvAccessor
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.client.ClientTickCounter
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color


class SpellPkg(var name: Text, var sprite: Identifier, var effect: Spell) {
    fun cast(world: World, user: PlayerEntity, hand: Hand, mod: SpellMod): SpellCastResult {
        return effect.cast(world, user, hand, mod)
    }
}

val spellReg: HashMap<Identifier, SpellPkg> = HashMap()
val spellInvReg: HashMap<Identifier, SpellInv> = HashMap()

fun registerSpell(id: Identifier, name: Text, sprite: Identifier, effect: Spell) {
    spellReg[id] = SpellPkg(name, sprite, effect)
}

fun mkMod(num: Int): SpellMod {
    return SpellMod.entries.filter { rank -> rank.v == num }[0]
}
enum class SpellMod(val v: Int) {
    EFF(3),
    PWR(2),
    AREA(1),
    NONE(0);
    fun pretty_name(): String? {
        return when (this) {
            EFF -> "Efficiency"
            PWR -> "Power"
            AREA -> "Area"
            NONE -> null
        }
    }
    fun model(): String {
        return when (this) {
            EFF -> "nova_arcana:item/mat-eff"
            PWR -> "nova_arcana:item/mat-pwr"
            AREA -> "nova_arcana:item/mat-area"
            NONE -> "nova_arcana:item/mat-blank"
        }
    }
}

enum class SpellCastResult {
    FAIL,
    SUCCESS
}

fun excavateParticle(s0: Float, s1: Float): WorldParticleBuilder {
    val startCol = Color(1, 153, 1)
    val edCol = Color(9, 249, 149)
    val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
    spawner.scaleData = GenericParticleData.create(s0, s1).build()
    spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
    spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
    spawner.setLifetime(40)
    spawner.enableNoClip()
    return spawner
}
fun supportParticle(s0: Float, s1: Float): WorldParticleBuilder {
    val startCol = Color(5, 104, 186)
    val edCol = Color(93, 239, 252)
    val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
    spawner.scaleData = GenericParticleData.create(s0, s1).build()
    spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
    spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
    spawner.setLifetime(40)
    spawner.enableNoClip()
    return spawner
}
fun dashParticle(s0: Float, s1: Float): WorldParticleBuilder {
    val startCol = Color(252, 242, 47)
    val edCol = Color(252, 249, 184)
    val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
    spawner.scaleData = GenericParticleData.create(s0, s1).build()
    spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
    spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
    spawner.setLifetime(40)
    //spawner.multiplyGravity(1.0f)
    spawner.enableNoClip()
    return spawner
}
fun recoverParticle(s0: Float, s1: Float): WorldParticleBuilder {
    val startCol = Color(246, 236, 236)
    val edCol = Color(255, 197, 197)
    val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
    spawner.scaleData = GenericParticleData.create(s0, s1).build()
    spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
    spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
    spawner.setLifetime(40)
    spawner.enableNoClip()
    return spawner
}
fun launchPlayer(user: PlayerEntity, pitch: Float, yaw: Float, roll: Float, speed: Float, divergence: Float) {
    val vec3d = user.velocity
    val f = -MathHelper.sin(yaw * (Math.PI / 180.0).toFloat()) * MathHelper.cos(pitch * (Math.PI / 180.0).toFloat())
    val g = -MathHelper.sin((pitch + roll) * (Math.PI / 180.0).toFloat())
    val h = MathHelper.cos(yaw * (Math.PI / 180.0).toFloat()) * MathHelper.cos(pitch * (Math.PI / 180.0).toFloat())
    playerProjectile(user, f.toDouble(), g.toDouble(), h.toDouble(), speed, divergence)

    user.velocity = user.velocity.add(vec3d)
}
fun playerProjectile(user: PlayerEntity, x: Double, y: Double, z: Double, speed: Float, divergence: Float) {
    val vec3d = Vec3d(x, y, z)
        .normalize()
        .add(
            user.random.nextTriangular(0.0, 0.0172275 * divergence.toDouble()),
            user.random.nextTriangular(0.0, 0.0172275 * divergence.toDouble()),
            user.random.nextTriangular(0.0, 0.0172275 * divergence.toDouble())
        )
        .multiply(speed.toDouble())
    user.velocity = vec3d
    /*
    val d = vec3d.horizontalLength()
    this.setYaw((MathHelper.atan2(vec3d.x, vec3d.z) * 180.0f / Math.PI.toFloat()).toFloat())
    this.setPitch((MathHelper.atan2(vec3d.y, d) * 180.0f / Math.PI.toFloat()).toFloat())
    this.prevYaw = this.getYaw()
    this.prevPitch = this.getPitch()
    */
}

fun interface Spell {
    fun cast(world: World, user: PlayerEntity, hand: Hand, mod: SpellMod): SpellCastResult
}
fun interface SpellInv {
    fun hover(stk: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean, mod: SpellMod)
}

fun regSpells() {
    val logger = LoggerFactory.getLogger("nova_arcana")
    registerSpell(Identifier("nova_arcana:spell/flame"),
        Text.literal("Flame"),
        Identifier("nova_arcana:item/mat-flame")
    ) { wld, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.FIREONLY)) {
                if (wld.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                if (wld.isClient) {
                    wld.playSound(
                        user,
                        user.blockPos,
                        SoundEvent.of(Identifier("item.firecharge.use"), 10.0F),
                        SoundCategory.PLAYERS
                    )
                    return@run SpellCastResult.SUCCESS
                }
                h.mana -= cost
            } else return@run SpellCastResult.FAIL

            wld.playSound(
                user,
                user.blockPos,
                SoundEvent.of(Identifier("item.firecharge.use"), 10.0F),
                SoundCategory.PLAYERS
            )
            for (i in 0..10) {
                val bolt = FireballProj(ModEntities.FireballProjType, user, wld)
                bolt.setNoGravity(true)
                bolt.setPosition(user.eyePos)
                if (mod == SpellMod.PWR) bolt.burntime = 6
                val maxLifespan = if (mod == SpellMod.AREA) -10 else 0
                bolt.setLifespan((maxLifespan..5).random())
                bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 1F, 20F)
                wld.spawnEntity(bolt)
            }
            h.syncMana()
            SpellCastResult.SUCCESS
        }
    }
    registerSpell(Identifier("nova_arcana:spell/siphon"), Text.literal("Siphon"), Identifier("nova_arcana:item/mat-siphon")
    ) { world, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.VOIDONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                if (world.isClient) {
                    return@run SpellCastResult.SUCCESS
                }
                h.mana -= cost
            } else return@run SpellCastResult.FAIL
            val bolt = DrainBeam(ModEntities.DrainBeamType, user, world)
            bolt.setNoGravity(true)
            bolt.setPosition(user.eyePos)
            if (mod == SpellMod.AREA) bolt.pierce = 2
            if (mod == SpellMod.PWR) bolt.dmg = 6F
            bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 1F, 0F)
            world.spawnEntity(bolt)
            h.syncMana()
            SpellCastResult.SUCCESS
        }
    }
    registerSpell(Identifier("nova_arcana:spell/excavate"), Text.literal("Excavate"), Identifier("nova_arcana:item/mat-excavate")
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
                    if (world.isClient) {
                        val spawner = excavateParticle(0.25f, 0.0f)
                        for (i in (0..5)) {
                            spawner.createBlockOutline(world, blockPos, world.getBlockState(blockPos))
                        }
                        return@run SpellCastResult.SUCCESS
                    }

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
    spellInvReg[Identifier("nova_arcana:spell/excavate")] = SpellInv {stk, world, entity, slot, selected, mod -> run {
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val cast = entity.raycast(reach, 0.0f, false)
        if (cast.type != HitResult.Type.BLOCK) return@run
        val hit = cast as BlockHitResult
        val spawner = excavateParticle(0.10f, 0.0f)
        spawner.createBlockOutline(world, hit.blockPos, world.getBlockState(hit.blockPos))
    }}
    registerSpell(Identifier("nova_arcana:spell/support"), Text.literal("Support"), Identifier("nova_arcana:item/mat-support")
    ) { world, user, hand, mod ->
        run {
            val cost = if (mod == SpellMod.EFF) 10 else 20
            val reach = if (mod == SpellMod.AREA) 20.0 else 10.0

            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.FROSTONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
            } else return@run SpellCastResult.FAIL
            val cast = user.raycast(reach, 0.0f, true)
            if (cast.type == HitResult.Type.BLOCK) {
                val blockHit = cast as BlockHitResult
                val dest = blockHit.blockPos.offset(blockHit.side)
                if (world.getBlockState(dest).isAir && !world.isClient) world.setBlockState(
                    dest,
                    ModBlocks.SUPPORT_BLOCK.defaultState
                )
                for (i in 0..5) supportParticle(0.25f, 0.0f).createBlockOutline(
                    world,
                    dest,
                    ModBlocks.SUPPORT_BLOCK.defaultState
                )
                h.mana -= cost
                h.syncMana()
                return@run SpellCastResult.SUCCESS
            } else return@run SpellCastResult.FAIL
        }
    }
    spellInvReg[Identifier("nova_arcana:spell/support")] = SpellInv {stk, world, entity, slot, selected, mod -> run {
        val reach = if (mod == SpellMod.AREA) 20.0 else 10.0
        val cast = entity.raycast(reach, 0.0f, true)
        if (cast.type != HitResult.Type.BLOCK) return@run
        val hit = cast as BlockHitResult
        val dest = hit.blockPos.offset(hit.side)
        supportParticle(0.10f, 0.0f).createBlockOutline(world, dest, ModBlocks.SUPPORT_BLOCK.defaultState)
    }}
    registerSpell(Identifier("nova_arcana:spell/dash"), Text.literal("Dash"), Identifier("nova_arcana:item/mat-dash")) { world, user, hand, mod ->
        run {
            if (user.isOnGround) return@run SpellCastResult.FAIL
            val cost = if (mod == SpellMod.EFF) 40 else 60
            val speed = if (mod == SpellMod.AREA) 2.5f else 1.5f
            val inv = if (mod == SpellMod.PWR) 15 else 10
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.AIRONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                h.mana -= cost
                h.syncMana()
            } else return@run SpellCastResult.FAIL
            launchPlayer(user, user.pitch, user.yaw, 0.0f, speed, 0.0f)
            for (i in 0..20) {
                val deviationX = (-10..10).random().toDouble() / 10
                val deviationY = (-10..10).random().toDouble() / 10 + 1
                val deviationZ = (-10..10).random().toDouble() / 10
                dashParticle(1.0f, 0.0f).spawn(world, user.x + deviationX, user.y + deviationY, user.z + deviationZ)
            }
            user.timeUntilRegen = inv
            (user as InvAccessor).lastDamageTaken = 20.0f
            return@run SpellCastResult.SUCCESS
        }
    }
    registerSpell(Identifier("nova_arcana:spell/recovery"), Text.literal("Recovery"), Identifier("nova_arcana:item/mat-recovery")
    ) { world, user, hand, mod ->
        run {
            val amount = if (mod == SpellMod.PWR) 2f else 1f
            val cost = if (mod == SpellMod.EFF) 20 else 40
            val area = if (mod == SpellMod.AREA) 3 else 1
            val h = ManaHandle(user)
            val stk = user.getStackInHand(hand)
            val rank = mkRank(stk.orCreateNbt.getInt("tier"))
            if (!rank.canCast(WandRank.SPIRITONLY)) {
                if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
                return@run SpellCastResult.FAIL
            }
            if (h.mana >= cost) {
                h.mana -= cost
                h.syncMana()
            } else return@run SpellCastResult.FAIL
            user.heal(amount)
            for (entity in world.getOtherEntities(user, Box(user.x - area, user.y, user.z - area, user.x + area, user.y + 1 + area, user.z + area))) {
                if (entity is LivingEntity) {
                    entity.heal(amount)
                }
            }
            recoverParticle(0.25f, 0.0f).repeatCircle(world, user.x, user.y+1, user.z, area.toDouble(), 40)
            return@run SpellCastResult.SUCCESS
        }
    }
    registerSpell(Identifier("nova_arcana:spell/substitute"), Text.literal("Substitute"), Identifier("nova_arcana:item/mat-blank")) {world, user, hand, mod -> run {
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
    spellInvReg[Identifier("nova_arcana:spell/substitute")] = SpellInv {stk, world, entity, slot, selected, mod -> run {
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