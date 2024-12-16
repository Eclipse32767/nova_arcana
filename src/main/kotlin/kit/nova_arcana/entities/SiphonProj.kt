package kit.nova_arcana.entities

import kit.nova_arcana.ManaHandle
import kit.nova_arcana.ModEntities
import kit.nova_arcana.fx.WispTrailEffects
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class DrainBeam: ThrownItemEntity {
    private var lifespan = 0
    var pierce = 1
    var dmg = 3F

    constructor(type: EntityType<DrainBeam>, world: World?): super(type, world)
    constructor(type: EntityType<DrainBeam>, owner: LivingEntity, world: World?): super(type, owner, world)

    fun setLifespan(a: Int) {
        lifespan = a
    }
    fun getLifespan(): Int {
        return lifespan
    }

    private fun spawnParticle() {
        if (world.isClient) {
            val fx = WispTrailEffects(Color(100, 0, 100), Color(0, 100, 200), pos)
            fx.startScale = 0.5f
            fx.motion = Vec3d(0.0, 0.01, 0.0)
            fx.spawn(world)
        }
    }

    override fun getDefaultItem(): Item {
        return Items.AIR;
    }

    override fun tick() {
        super.tick()
        spawnParticle()
        lifespan++
        if (lifespan > 100) {
            kill()
        }
    }
    override fun onEntityHit(hitResult: EntityHitResult) {
        if (this.world.isClient) {
            return
        }
        super.onEntityHit(hitResult);
        val source = DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.MAGIC), owner)
        hitResult.entity.damage(source, dmg)
        pierce--
        if (owner != null && owner is PlayerEntity) {
            val bolt = SiphonHeal(ModEntities.SiphonHealType, owner as PlayerEntity, world)
            bolt.setPosition(pos)
            bolt.setNoGravity(true)
            world.spawnEntity(bolt)
        }

        if (pierce < 1) {
            kill()
        }
    }

    override fun onBlockHit(hitResult: BlockHitResult) {
        if (this.world.isClient) {
            return
        }
        super.onBlockHit(hitResult)
        kill()
    }
}
class SiphonHeal: ThrownItemEntity {
    private var lifespan = 0

    constructor(type: EntityType<SiphonHeal>, world: World?): super(type, world)
    constructor(type: EntityType<SiphonHeal>, owner: LivingEntity, world: World?): super(type, owner, world)

    fun setLifespan(a: Int) {
        lifespan = a
    }
    fun getLifespan(): Int {
        return lifespan
    }

    private fun spawnParticle() {
        if (world.isClient) {
            val fx = WispTrailEffects(Color(246, 236, 236), Color(255, 197, 197), pos)
            fx.startScale = 0.5f
            fx.motion = Vec3d(0.0, 0.01, 0.0)
            fx.spawn(world)
        }
    }

    override fun getDefaultItem(): Item {
        return Items.AIR;
    }
    fun mvTowardTrgt(trgt: Vec3d) {
        val diff = trgt - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    override fun tick() {
        super.tick()
        spawnParticle()
        if (this.world.isClient) {
            return
        }
        lifespan++
        if (lifespan > 500) {
            kill()
        }
        if (owner != null) {
            var target = owner!!.pos
            target = target.add(0.0, 1.0, 0.0)
            mvTowardTrgt(target)
            if (this.pos.distanceTo(target) <= 1.0) {
                if (owner is PlayerEntity) {
                    val plr = owner as PlayerEntity
                    val h = ManaHandle(plr)
                    h.mana = minOf(h.mana+30, h.manacap)
                    h.syncMana()
                }
                kill()
            }
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult?) {
    }
    override fun onBlockHit(blockHitResult: BlockHitResult?) {
    }
}