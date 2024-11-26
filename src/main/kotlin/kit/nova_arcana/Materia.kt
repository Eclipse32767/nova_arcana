package kit.nova_arcana

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.FireworkStarRecipe
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun mkMateria(spell: String): ItemStack {
    val stk = ModItems.materia.defaultStack
    val nbt = stk.orCreateNbt
    nbt.putString("spell", spell)
    return stk
}
fun mkModMateria(mod: SpellMod, model: String): ItemStack {
    val stk = ModItems.modMateria.defaultStack
    val nbt = stk.orCreateNbt
    nbt.putString("model", model)
    nbt.putInt("modifier", mod.v)
    return stk
}

class MateriaItem(s: FabricItemSettings): Item(s) {
    override fun getName(stack: ItemStack): Text {
        val nbt = stack.orCreateNbt
        val fetchedname = spellReg[Identifier(nbt.getString("spell"))]?.name
        return fetchedname?: Text.literal("")
    }
}
class ModMateria(s: FabricItemSettings): Item(s) {
    override fun getName(stack: ItemStack): Text {
        val nbt = stack.orCreateNbt
        val fetchedname = mkMod(nbt.getInt("modifier")).pretty_name()
        return Text.literal("Spell Modifier: ${fetchedname?: "ERROR"}")
    }
}