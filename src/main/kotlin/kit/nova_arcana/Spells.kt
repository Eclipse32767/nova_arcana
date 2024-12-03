package kit.nova_arcana


import kit.nova_arcana.entities.*
import kit.nova_arcana.items.WandRank
import kit.nova_arcana.items.mkRank
import kit.nova_arcana.mixin.InvAccessor
import kit.nova_arcana.spells.*
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
    regFlame(logger)
    regSupport(logger)
    regExcavate(logger)
    regDash(logger)
    regRecovery(logger)
    regSiphon(logger)

    regSubstitute(logger)
    regImmolate(logger)
    regMalevolence(logger)


    registerSpell(Identifier("nova_arcana:spell/overclock"), Text.literal("Overclock"), Identifier("nova_arcana:item/mat-blank")) { world, user, hand, mod -> run {
        if (user.getStatusEffect(ModEffects.FAIR_GROUND) != null) return@run SpellCastResult.FAIL
        user.addStatusEffect(StatusEffectInstance(ModEffects.OVERCLOCKED, 300, 1))
        user.addStatusEffect(StatusEffectInstance(ModEffects.FAIR_GROUND, 1200, 0))
        for (i in 0..20) recoverParticle(0.1f, 0.0f).spawnLine(world, user.pos, user.pos.add(10.0, 0.0, 0.0))
        return@run SpellCastResult.SUCCESS
    }}
}