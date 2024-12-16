package kit.nova_arcana.entities

import kit.nova_arcana.fx.WispTrailEffects
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import java.awt.Color

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
        if (this.world.isClient) {
            WispTrailEffects(false, Color(1, 153, 1), Color(9, 249, 149), 0.5f, 40, pos).spawn(world)
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