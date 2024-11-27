package kit.nova_arcana.armor

import kit.nova_arcana.client.CloakArmorRenderer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.RenderProvider
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState
import java.util.function.Consumer
import java.util.function.Supplier

class CloakArmorItem(material: MagicArmorMaterial, type: Type, settings: FabricItemSettings) : MagicArmor(material, type, settings), GeoItem {
    val cache = SingletonAnimatableInstanceCache(this)
    val tRenderProvider = GeoItem.makeRenderer(this)

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(AnimationController(this, "controller", this::predicate))
    }
    fun predicate(animationState: AnimationState<CloakArmorItem>): PlayState {
        animationState.controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP))
        return PlayState.CONTINUE
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return cache
    }

    override fun createRenderer(consumer: Consumer<Any>) {
        consumer.accept(object: RenderProvider {
            val renderer = CloakArmorRenderer()
            override fun getHumanoidArmorModel(
                livingEntity: LivingEntity,
                itemStack: ItemStack,
                equipmentSlot: EquipmentSlot,
                original: net.minecraft.client.render.entity.model.BipedEntityModel<LivingEntity>
            ): net.minecraft.client.render.entity.model.BipedEntityModel<LivingEntity> {
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original)
                return this.renderer
            }
        })
    }

    override fun getRenderProvider(): Supplier<Any> {
        return tRenderProvider
    }
}
