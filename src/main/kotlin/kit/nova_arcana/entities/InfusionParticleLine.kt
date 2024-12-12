package kit.nova_arcana.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

private val START_SCALE = DataTracker.registerData(InfusionParticleLine::class.java, TrackedDataHandlerRegistry.FLOAT)
private val COLOR1 = DataTracker.registerData(InfusionParticleLine::class.java, TrackedDataHandlerRegistry.INTEGER)
private val COLOR2 = DataTracker.registerData(InfusionParticleLine::class.java, TrackedDataHandlerRegistry.INTEGER)
private val logger = LoggerFactory.getLogger("help")

class InfusionParticleLine(type: EntityType<InfusionParticleLine>, world: World, val dest: Vec3d): ThrownItemEntity(type, world) {
    var color1: Color
        get() = Color(dataTracker.get(COLOR1))
        set(value) = dataTracker.set(COLOR1, value.rgb)
    var color2: Color
        get() = Color(dataTracker.get(COLOR2))
        set(value) = dataTracker.set(COLOR2, value.rgb)
    var startScale: Float
        get() = dataTracker.get(START_SCALE)
        set(value) = dataTracker.set(START_SCALE, value)
    var lifespan = 2000
    fun particleSpawner(): WorldParticleBuilder {
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(startScale, 0.0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(color1, color2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(40)
        spawner.enableNoClip()
        return spawner
    }
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    fun mvTowardTrgt() {
        val diff = dest - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.25F, 0.0F)
    }
    override fun tick() {
        super.tick()
        //setNoGravity(true)
        //mvTowardTrgt()
        //val logger = LoggerFactory.getLogger("hhhh")
        //logger.atInfo().log("${velocity.x}, ${velocity.y}, ${velocity.z}")
        particleSpawner().spawn(world, x, y, z)
        lifespan--
        if (lifespan <= 0) kill()
        if (pos.distanceTo(dest) < 0.5) kill()
    }
    override fun initDataTracker() {
        super.initDataTracker()
        //logger.atInfo().log("initializing the data tracker")
        dataTracker.startTracking(START_SCALE, 0.75f)
        dataTracker.startTracking(COLOR1, Color.WHITE.rgb)
        dataTracker.startTracking(COLOR2, Color.WHITE.rgb)
    }
}