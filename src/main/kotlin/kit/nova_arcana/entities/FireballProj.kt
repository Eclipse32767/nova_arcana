package kit.nova_arcana.entities

import kit.nova_arcana.ModBlocks
import kit.nova_arcana.fx.WispTrailEffects
import net.minecraft.block.Blocks
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
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

class FireballProj : ThrownItemEntity {
    private var lifespan = 0
    private var pierce = 3
    var damage = 0F
    var burntime = 3

    constructor(type: EntityType<FireballProj>, world: World?): super(type, world)
    constructor(type: EntityType<FireballProj>, owner: LivingEntity, world: World?): super(type, owner, world)

    fun setLifespan(a: Int) {
        lifespan = a
    }
    fun getLifespan(): Int {
        return lifespan
    }

    private fun spawnParticle() {
        if (!world.isClient) return
        val fx = WispTrailEffects(Color(252, 167, 63), Color(252, 113, 63), pos)
        fx.smoke = true
        fx.motion = Vec3d(0.0, 0.01, 0.0)
        fx.startScale = 0.5f
        fx.spawn(world)
    }

    override fun getDefaultItem(): Item {
        return Items.AIR;
    }

    override fun tick() {
        super.tick()

        //world.addParticle(ParticleTypes.FLAME, false, x, y, z, 0.0, -0.1, 0.0)
        spawnParticle()
        lifespan++
        if (lifespan > 15) {
            kill()
        }
    }
    override fun onEntityHit(hitResult: EntityHitResult) {
        if (this.world.isClient) {
            return
        }
        super.onEntityHit(hitResult);
        val source =
            DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.FIREBALL), owner)
        hitResult.entity.damage(source, damage)
        hitResult.entity.setOnFireFor(burntime)
        pierce--
        if (pierce < 1) {
            kill()
        }
    }

    override fun onBlockHit(hitResult: BlockHitResult) {
        if (this.world.isClient) {
            return
        }
        super.onBlockHit(hitResult)
        if (world.getBlockState(hitResult.blockPos).block == ModBlocks.SUPPORT_BLOCK) {
            world.setBlockState(hitResult.blockPos, Blocks.AIR.defaultState)
        }
        kill()
    }
}