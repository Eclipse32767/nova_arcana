package kit.nova_arcana.armor

import kit.nova_arcana.ManaHandle
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

open class MagicArmor(val mMaterial: MagicArmorMaterial, type: Type, settings: FabricItemSettings): ArmorItem(mMaterial, type, settings) {
    /*
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (entity !is PlayerEntity) return
        var manaBoost = 0
        val armors = listOf(entity.inventory.getArmorStack(3), entity.inventory.getArmorStack(2), entity.inventory.getArmorStack(1), entity.inventory.getArmorStack(0))
       for (armor in armors) {
           val item = armor.item
           if (item is MagicArmor) {
               manaBoost += item.getManaCapBoost()
           }
       }
        val h = ManaHandle(entity)
        h.manacap = 100 + manaBoost


        super.inventoryTick(stack, world, entity, slot, selected)
    }
     */
    fun getManaCapBoost(): Int {
        return this.mMaterial.getManaCapBonus(type)
    }
    fun getManaRegenBoost(): Int {
        return this.mMaterial.getManaRegenBonus(type)
    }
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += Text.literal("Mana Cap: +${mMaterial.getManaCapBonus(type)}")
        tooltip += Text.literal("Mana Regen: +${mMaterial.getManaRegenBonus(type)}")
    }
}