package kit.nova_arcana

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

class WandCore(settings: FabricItemSettings): Item(settings) {
    override fun getName(stk: ItemStack): Text {
        var str = stk.orCreateNbt.getString("name")
        if (str == "") str = "Staff Core"
        return Text.literal(str)
    }
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        val nbt = stack?.orCreateNbt
        super.appendTooltip(stack, world, tooltip, context)
        if (tooltip != null) {
            val slots = (nbt?.getInt("slots_f") ?: 0) * 2 + (nbt?.getInt("slots_s") ?: 0)
            tooltip += Text.literal("Materia Slots: $slots")
        }
    }
}
class WandDecor(settings: FabricItemSettings): Item(settings) {
    override fun getName(stk: ItemStack): Text {
        var str = stk.orCreateNbt.getString("name")
        if (str == "") str = "Staff Decoration"
        return Text.literal(str)
    }
}

class WandGem(settings: FabricItemSettings): Item(settings) {
    override fun getName(stk: ItemStack): Text {
        var str = stk.orCreateNbt.getString("name")
        if (str == "") str = "Staff Gem"
        return Text.literal("Staff Gem: $str")
    }
}