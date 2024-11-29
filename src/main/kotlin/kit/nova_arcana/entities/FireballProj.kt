package kit.nova_arcana.entities

import kit.nova_arcana.ModBlocks
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
        val startCol = Color(252, 167, 63)
        val edCol = Color(252, 113, 63)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.SMOKE_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.5f, 0F).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol)
            .setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        //spawner.spinData = SpinParticleData.create(0.2f, 0.4f).setSpinOffset((world.time * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build()
        spawner.setLifetime(40)
        spawner.addMotion(0.0, 0.01, 0.0)
        spawner.enableNoClip()
        spawner.spawn(world, pos.x, pos.y, pos.z)
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