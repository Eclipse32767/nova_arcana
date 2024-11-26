package kit.nova_arcana

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.world.World

class SurrogateItem(private val replacement: ItemStack): Item(FabricItemSettings().maxCount(1)) {
    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (entity is PlayerEntity) {
            entity.inventory.setStack(slot, replacement.copy())
        }
    }
    override fun getName(stack: ItemStack): Text {
        return Text.literal("§k The Forbidden")
    }
}