package kit.nova_arcana.entities

import kit.nova_arcana.ModEntities
import kit.nova_arcana.SpellMod
import kit.nova_arcana.fx.WispTrailEffects
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class ImmolateProj: ThrownItemEntity {
    constructor(type: EntityType<ImmolateProj>, world: World): super(type, world)
    constructor(type: EntityType<ImmolateProj>, owner: LivingEntity, world: World): super(type, owner, world)
    var lifespan = 50
    var damage = 1f
    val burnTime = 3
    fun getParticle(s1: Float, s2: Float): WispTrailEffects {
        val fx = WispTrailEffects(Color(252, 167, 63), Color(252, 113, 63), pos)
        fx.motion = Vec3d(0.0, 0.01, 0.0)
        fx.smoke = true
        fx.startScale = s1
        return fx
    }
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun fireball() {
        if (owner == null) {
            this.kill()
            return
        }
        if (world.isClient) for (i in 0..5) getParticle(5f, 0f).spawn(world)
        for (entity in this.world.getOtherEntities(this, Box.of(pos, 5.0, 5.0, 5.0))) {
            val source =
                DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.FIREBALL), owner)
            entity.damage(source, damage)
            entity.setOnFireFor(burnTime)
        }
        for (i in 0..20) {
            val bolt = FireballProj(ModEntities.FireballProjType, owner as LivingEntity, world)
            bolt.setNoGravity(true)
            bolt.setPosition(pos)
            bolt.setLifespan((-5..5).random())
            bolt.burntime = 6
            val range = (-10..10)
            bolt.setVelocity(range.random().toDouble(), range.random().toDouble(), range.random().toDouble(), 0.25F, 20F)
            world.spawnEntity(bolt)
        }
        this.kill()
    }

    override fun tick() {
        super.tick()
        if (world.isClient) getParticle(0.5f, 0.0f).spawn(world)
        lifespan--
        if (lifespan <= 0) {
            fireball()
        }
    }
    override fun onEntityHit(entityHitResult: EntityHitResult) {
        fireball()
    }
    override fun onBlockHit(blockHitResult: BlockHitResult) {
        fireball()
    }
}