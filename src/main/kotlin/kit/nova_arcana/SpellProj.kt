package kit.nova_arcana

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import team.lodestar.lodestone.handlers.FireEffectHandler
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.fireeffect.FireEffectInstance
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color
import kotlin.math.atan
import kotlin.math.sqrt

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
        spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
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
        val source = DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.FIREBALL), owner)
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
        val startCol = Color(100, 0, 100)
        val edCol = Color(0, 100, 200)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.5f, 0F).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
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
        val startCol = Color(246, 236, 236)
        val edCol = Color(255, 197, 197)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.5f, 0F).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        //spawner.spinData = SpinParticleData.create(0.2f, 0.4f).setSpinOffset((world.time * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build()
        spawner.setLifetime(40)
        spawner.addMotion(0.0, 0.01, 0.0)
        spawner.enableNoClip()
        spawner.spawn(world, pos.x, pos.y, pos.z)
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

class ExcavateItem: ThrownItemEntity {
    constructor(type: EntityType<ExcavateItem>, world: World): super(type, world)
    constructor(type: EntityType<ExcavateItem>, owner: LivingEntity, world: World): super(type, owner, world)
    var lifespan = 0
    var carryStack: ItemStack? = null
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    /*
    fun setLifespan(a: Int) {
        lifespan = a
    }
    fun getLifespan(): Int {
        return lifespan
    }
     */
    fun setStk(itemStack: ItemStack) {
        carryStack = itemStack
    }

    private fun spawnParticle() {
        val startCol = Color(1, 153, 1)
        val edCol = Color(9, 249, 149)
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(0.50f, 0F).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(startCol, edCol).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(40)
        spawner.enableNoClip()
        spawner.spawn(world, pos.x, pos.y, pos.z)
    }

    fun mvTowardTrgt(trgt: Vec3d) {
        val diff = trgt - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    fun drop() {
        if (carryStack != null) {
            val e = ItemEntity(EntityType.ITEM, world)
            e.stack = carryStack
            e.setPosition(pos)
            e.velocity = velocity
            world.spawnEntity(e)
        }

        kill()
    }
    override fun tick() {
        super.tick()
        spawnParticle()
        if (this.world.isClient) {
            return
        }
        lifespan++
        if (lifespan > 500) {
            drop()
        }
        if (owner != null) {
            var target = owner!!.pos
            target = target.add(0.0, 1.0, 0.0)
            mvTowardTrgt(target)
            if (this.pos.distanceTo(target) <= 1.0) {
                drop()
            }
        }
    }
}


private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private operator fun BlockPos.plus(scalar: Int): BlockPos {
    return BlockPos(x+scalar, y+scalar, z+scalar)
}

private operator fun BlockPos.minus(scalar: Int): BlockPos {
    return BlockPos(x - scalar, y - scalar, z - scalar)
}
private operator fun BlockPos.minus(o: BlockPos): BlockPos {
    return BlockPos(x - o.x, y - o.y, z - o.z)
}