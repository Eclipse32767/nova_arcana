package kit.nova_arcana.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Vector3f
import org.slf4j.LoggerFactory
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

private val START_SCALE = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.FLOAT)
private val COLOR1 = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.INTEGER)
private val COLOR2 = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.INTEGER)
private val DEST = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.VECTOR3F)
private val PARTICLE_LIFESPAN = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.INTEGER)
private val EFFECT_LIFESPAN = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.INTEGER)
private val DENSITY = DataTracker.registerData(ManaBeam::class.java, TrackedDataHandlerRegistry.INTEGER)
private val logger = LoggerFactory.getLogger("help")

class ManaBeam(type: EntityType<ManaBeam>, world: World): ThrownItemEntity(type, world) {
    var color1: Color
        get() = Color(dataTracker.get(COLOR1))
        set(value) = dataTracker.set(COLOR1, value.rgb)
    var color2: Color
        get() = Color(dataTracker.get(COLOR2))
        set(value) = dataTracker.set(COLOR2, value.rgb)
    var startScale: Float
        get() = dataTracker.get(START_SCALE)
        set(value) = dataTracker.set(START_SCALE, value)
    var dest: Vec3d
        get() {
            val ret = dataTracker.get(DEST)
            return Vec3d(ret.x.toDouble(), ret.y.toDouble(), ret.z.toDouble())
        }
        set(value) = dataTracker.set(DEST, Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat()))
    var particleLifespan: Int
        get() = dataTracker.get(PARTICLE_LIFESPAN)
        set(value) = dataTracker.set(PARTICLE_LIFESPAN, value)
    var lifespan: Int
        get() = dataTracker.get(EFFECT_LIFESPAN)
        set(value) = dataTracker.set(EFFECT_LIFESPAN, value)
    var density: Int
        get() = dataTracker.get(DENSITY)
        set(value) = dataTracker.set(DENSITY, value)
    fun particleSpawner(): WorldParticleBuilder {
        val spawner = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        spawner.scaleData = GenericParticleData.create(startScale, 0.0f).build()
        spawner.transparencyData = GenericParticleData.create(0.75F, 0.25F).build()
        spawner.colorData = ColorParticleData.create(color1, color2).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build()
        spawner.setLifetime(particleLifespan)
        spawner.enableNoClip()
        return spawner
    }
    override fun getDefaultItem(): Item {
        return Items.AIR
    }
    override fun tick() {
        super.tick()
        //setNoGravity(true)
        //mvTowardTrgt()
        //val logger = LoggerFactory.getLogger("hhhh")
        //logger.atInfo().log("${velocity.x}, ${velocity.y}, ${velocity.z}")
        for (i in 0..density) particleSpawner().spawnLine(world, pos, dest)
        if (world.isClient) return
        lifespan--
        if (lifespan < 0) kill()
    }
    override fun initDataTracker() {
        super.initDataTracker()
        //logger.atInfo().log("initializing the data tracker")
        dataTracker.startTracking(START_SCALE, 0.75f)
        dataTracker.startTracking(COLOR1, Color.WHITE.rgb)
        dataTracker.startTracking(COLOR2, Color.WHITE.rgb)
        dataTracker.startTracking(DEST, Vector3f(0f, 0f, 0f))
        dataTracker.startTracking(PARTICLE_LIFESPAN, 40)
        dataTracker.startTracking(EFFECT_LIFESPAN, 20)
        dataTracker.startTracking(DENSITY, 5)
    }
}