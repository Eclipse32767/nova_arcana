package kit.nova_arcana.entities

import kit.nova_arcana.dashParticle
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class MagicMissile: ThrownItemEntity {
    constructor(type: EntityType<MagicMissile>, world: World): super(type, world)
    constructor(type: EntityType<MagicMissile>, owner: LivingEntity, world: World): super(type, owner, world)
    override fun getDefaultItem(): Item = Items.AIR
    var lifespan = 300
    val dmg = 3f
    val logger = LoggerFactory.getLogger("magic-missile")
    override fun tick() {
        super.tick()
        dashParticle(0.5f, 0.0f).spawn(world, x, y, z)
        if (!world.isClient) {
            lifespan--
            if (lifespan < 0) kill()
        }
        if (owner != null) {
            //logger.atInfo().log("retargeting")
            if (owner!!.pos.distanceTo(pos) > 70) {
                mvTowardTrgt(owner!!.pos)
                return
            }
            val dest = owner!!.raycast(70.0, 0.0f, false)
            val maxDist = 200.0
            val cameraPos = owner!!.getCameraPosVec(0f)
            val rot = owner!!.getRotationVec(0f)
            val ctx = cameraPos.add(rot.x * maxDist, rot.y * maxDist, rot.z * maxDist)
            val entityCast = ProjectileUtil.raycast(owner!!, cameraPos, ctx, Box.of(owner!!.pos, maxDist*2, maxDist*2, maxDist*2), {!it.isSpectator && it.canHit() && it is LivingEntity}, maxDist)
            if (entityCast != null) {
                //logger.atInfo().log("hit entity")
                val head = entityCast.entity.eyePos
                val feet = entityCast.entity.pos
                val total = head.add(feet)
                val mid = Vec3d(total.x/2, total.y/2, total.z/2)
                mvTowardTrgt(mid)
            } else if (dest.type == HitResult.Type.BLOCK) {
                val blk = dest as BlockHitResult
                mvTowardTrgt(blk.pos.offset(blk.side, 0.5))
            } else mvTowardTrgt(dest.pos)
        }
    }
    fun mvTowardTrgt(trgt: Vec3d) {
        val diff = trgt - pos
        this.setVelocity(diff.x, diff.y, diff.z, 0.5F, 0.0F)
    }
    override fun onEntityHit(hitResult: EntityHitResult) {
        if (world.isClient) return
        super.onEntityHit(hitResult)
        val source = DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.MAGIC), owner)
        if (hitResult.entity.damage(source, dmg)) kill()
    }
    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        //kill()
    }
}