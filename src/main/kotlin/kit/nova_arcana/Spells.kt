package kit.nova_arcana


import kit.nova_arcana.entities.*
import kit.nova_arcana.fx.OutlineEffects
import kit.nova_arcana.fx.RingEffects
import kit.nova_arcana.fx.WispTrailEffects
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import kit.nova_arcana.mixin.InvAccessor
import kit.nova_arcana.networking.fx.OutlineNetEffect
import kit.nova_arcana.networking.fx.RingNetEffect
import kit.nova_arcana.spells.*
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
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
fun registerSpell(id: String, name: String, sprite: String, effect: Spell) {
    registerSpell(Identifier(id), Text.literal(name), Identifier(sprite), effect)
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

fun excavateParticle(s0: Float, pos: BlockPos, st: BlockState, density: Int): OutlineEffects {
    val fx = OutlineEffects(Color(1, 153, 1), Color(9, 249, 149), density, 40, s0, st, pos)
    return fx
}
fun excavateParticlePacket(s0: Float, pos: BlockPos, full: Boolean, density: Int): OutlineNetEffect {
    val fx = OutlineNetEffect(Color(1, 153, 1), Color(9, 249, 149), density, 40, s0, full, pos)
    return fx
}

fun supportParticle(s0: Float, pos: BlockPos, st: BlockState, density: Int): OutlineEffects {
    val fx = OutlineEffects(Color(5, 104, 186), Color(93, 239, 252), density, 40, s0, st, pos)
    return fx
}
fun supportParticlePacket(s0: Float, pos: BlockPos, full: Boolean, density: Int): OutlineNetEffect {
    val fx = OutlineNetEffect(Color(5, 104, 186), Color(93, 239, 252), density, 40, s0, full, pos)
    return fx
}

fun dashParticle(s0: Float, pos: Vec3d): WispTrailEffects {
    val fx = WispTrailEffects(Color(252, 242, 47), Color(252, 249, 184), pos)
    fx.startScale = s0
    return fx
}
fun recoverParticle(s0: Float, pos: Vec3d, rad: Double, density: Int): RingEffects {
    val fx = RingEffects(Color(246, 236, 236), Color(255, 197, 197), 40, density, s0, pos, rad)
    fx.motion = Vec3d(0.0, 0.1, 0.0)
    return fx
}
fun recoverParticlePacket(s0: Float, pos: Vec3d, rad: Double, density: Int): RingNetEffect {
    return RingNetEffect(Color(246, 236, 236), Color(255, 197, 197), 40, density, s0, pos, rad, Vec3d(0.0, 0.1, 0.0))
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
    regFlame(logger)
    regSupport(logger)
    regExcavate(logger)
    regDash(logger)
    regRecovery(logger)
    regSiphon(logger)

    regSubstitute(logger)
    regImmolate(logger)
    regMalevolence(logger)


    registerSpell(Identifier("nova_arcana:spell/overclock"), Text.literal("Overclock"), Identifier("nova_arcana:item/mat-overclock")) { world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 40 else 60
        val pwr = if (mod == SpellMod.PWR) 2 else 1
        val area = if (mod == SpellMod.AREA) 6.0 else 3.0
        val h = ManaHandle(user)
        val stk = user.getStackInHand(hand)
        val rank = mkRank(stk.orCreateNbt.getInt("tier"))
        if (!rank.canCast(WandRank.TIER1)) {
            if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
            return@run SpellCastResult.FAIL
        }
        if (h.mana >= cost) {
        } else return@run SpellCastResult.FAIL
        val targets = world.getOtherEntities(null, Box.of(user.pos, area, area, area))
        if (!world.isClient) {
            val packet = RingNetEffect(Color(100, 0, 100), Color(0, 100, 200), 40, 20, 0.5f, user.pos, area/2, Vec3d(0.0, 0.1, 0.0))
            for (plr in PlayerLookup.tracking(user).filter { it != user }) {
                packet.sendTo(plr)
            }
            packet.sendTo(user as ServerPlayerEntity)
        }
        for (target in targets) {
            if (target is LivingEntity) {
                if (target.getStatusEffect(ModEffects.FAIR_GROUND) != null) continue
                target.addStatusEffect(StatusEffectInstance(ModEffects.OVERCLOCKED, 300, pwr))
                target.addStatusEffect(StatusEffectInstance(ModEffects.FAIR_GROUND, 1200, 0))
            }
        }
        h.mana -= cost
        h.syncMana()
        return@run SpellCastResult.SUCCESS
    }}
    registerSpell("nova_arcana:spell/magic-missile", "Magic Missile", "nova_arcana:item/mat-blank") {world, user, hand, mod -> run {
        val cost = if (mod == SpellMod.EFF) 40 else 60
        val h = ManaHandle(user)
        val stk = user.getStackInHand(hand)
        val rank = mkRank(stk.orCreateNbt.getInt("tier"))
        if (!rank.canCast(WandRank.TIER1)) {
            if (world.isClient) user.sendMessage(Text.literal("The Staff appears incompatible with this spell. How odd."))
            return@run SpellCastResult.FAIL
        }
        if (h.mana >= cost) {
        } else return@run SpellCastResult.FAIL
        if (world.isClient) return@run SpellCastResult.SUCCESS
        val bolt = MagicMissile(ModEntities.MagicMissileType, user, world)
        bolt.setNoGravity(true)
        bolt.setPosition(user.eyePos)
        bolt.setVelocity(user, user.pitch, user.yaw, 0.0F, 0.5F, 0F)
        bolt.dmg = if(mod == SpellMod.PWR) 6f else 3f
        bolt.lifespan = if(mod == SpellMod.AREA) 300 else 150
        world.spawnEntity(bolt)
        h.mana -= cost
        h.syncMana()
        return@run SpellCastResult.SUCCESS
    }}
}