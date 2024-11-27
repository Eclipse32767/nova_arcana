package kit.nova_arcana.armor

import com.google.common.base.Supplier
import kit.nova_arcana.ModItems
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorItem.Type
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import java.rmi.registry.Registry

interface MagicArmorMaterial: ArmorMaterial {
    fun getManaCapBonus(type: Type): Int;
    fun getManaRegenBonus(type: Type): Int;
}

class CloakArmorMaterial: MagicArmorMaterial {
    override fun getDurability(type: Type): Int {
        return 400
    }
    override fun getManaCapBonus(type: Type): Int {
        return when(type) {
            Type.HELMET -> 6
            Type.CHESTPLATE -> 7
            Type.LEGGINGS -> 7
            Type.BOOTS -> 0
        }
    }

    override fun getManaRegenBonus(type: Type): Int {
        return 0
    }
    override fun getProtection(type: Type): Int {
        return when (type) {
            Type.HELMET -> 1
            Type.CHESTPLATE -> 3
            Type.LEGGINGS -> 2
            Type.BOOTS -> 1
        }
    }
    override fun getEnchantability(): Int {
        return 20
    }
    override fun getEquipSound(): SoundEvent {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    }
    override fun getRepairIngredient(): Ingredient {
        return Ingredient.ofStacks(Items.DIAMOND.defaultStack)
    }
    override fun getName(): String {
        return "cloak"
    }
    override fun getToughness(): Float {
        return 3f
    }
    override fun getKnockbackResistance(): Float {
        return 0f
    }
    companion object {
        val INSTANCE = CloakArmorMaterial()
    }
}
class EminenceArmorMaterial: MagicArmorMaterial {
    override fun getDurability(type: Type): Int {
        return 400
    }
    override fun getManaCapBonus(type: Type): Int {
        return when(type) {
            Type.HELMET -> 200
            Type.CHESTPLATE -> 0
            Type.LEGGINGS -> 0
            Type.BOOTS -> 0
        }
    }

    override fun getManaRegenBonus(type: Type): Int {
        return 16
    }
    override fun getProtection(type: Type): Int {
        return when (type) {
            Type.HELMET -> 1
            Type.CHESTPLATE -> 3
            Type.LEGGINGS -> 2
            Type.BOOTS -> 1
        }
    }
    override fun getEnchantability(): Int {
        return 20
    }
    override fun getEquipSound(): SoundEvent {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    }
    override fun getRepairIngredient(): Ingredient {
        return Ingredient.ofStacks(Items.DIAMOND.defaultStack)
    }
    override fun getName(): String {
        return "cloak"
    }
    override fun getToughness(): Float {
        return 3f
    }
    override fun getKnockbackResistance(): Float {
        return 0f
    }
    companion object {
        val INSTANCE = EminenceArmorMaterial()
    }
}