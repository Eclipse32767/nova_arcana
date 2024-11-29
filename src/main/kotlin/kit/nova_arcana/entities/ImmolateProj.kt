package kit.nova_arcana.entities

import kit.nova_arcana.ModEntities
import kit.nova_arcana.SpellMod
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
    val particleMain = getParticle(0.5f, 0f)
    val particleFireball = getParticle(5f, 0f)
    fun getParticle(s1: Float, s2: Float): WorldParticleBuilder {
        val startCol = Color(252, 167, 63)
        val edCol = Color(252, 113, 63)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.SMOKE_PARTICLE)
        spawner.scaleData = GenericParticleData.create(s1, s2).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol)
            .setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        //spawner.spinData = SpinParticleData.create(0.2f, 0.4f).setSpinOffset((world.time * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build()
        spawner.setLifetime(40)
        spawner.addMotion(0.0, 0.01, 0.0)
        spawner.enableNoClip()
        return spawner
    }
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun fireball() {
        if (owner == null) {
            this.kill()
            return
        }
        particleFireball.setForceSpawn(true).repeat(world, x, y, z, 5)
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
        particleMain.spawn(world, x, y, z)
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