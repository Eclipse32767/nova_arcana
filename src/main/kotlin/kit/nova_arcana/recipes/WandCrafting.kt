package kit.nova_arcana.recipes

import kit.nova_arcana.ModItems
import kit.nova_arcana.Recipes
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.LoggerFactory


class WandCrafting(id: Identifier, category: CraftingRecipeCategory): SpecialCraftingRecipe(id, category) {
    val logger = LoggerFactory.getLogger("nova_arcana")
    override fun matches(inventory: RecipeInputInventory, world: World): Boolean {
        //logger.atInfo().log("checking for match...")
        val stks = inventory.inputStacks
        var cores = 0
        var decors = 0
        var gems = 0
        for (stk in stks) {
            val item = stk.item
            when (item) {
                ModItems.wandCore -> cores++
                ModItems.wandDecor -> decors++
                ModItems.wandGem -> gems++
                else -> {
                    if (!stk.isEmpty) return false
                }
            }
        }
        return (cores == 1 && decors == 1 && gems == 1)
    }

    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager): ItemStack {
        val wand = ModItems.wand.defaultStack
        val nbt = wand.orCreateNbt
        for (stk in inventory.inputStacks){
            val o = stk.orCreateNbt
            when (stk.item) {
                ModItems.wandCore -> {
                    nbt.putString("core", o.getString("model"))
                    nbt.putInt("slots_s", o.getInt("slots_s"))
                    nbt.putInt("slots_f", o.getInt("slots_f"))
                }
                ModItems.wandDecor -> {
                    nbt.putString("decor", o.getString("model"))
                }
                ModItems.wandGem -> {
                    nbt.putString("gem", o.getString("model"))
                    nbt.putInt("tier", o.getInt("rank"))
                    nbt.putString("name", "${o.getString("name")} Staff")
                }
            }
        }
        return wand
    }

    override fun fits(width: Int, height: Int): Boolean {
        logger.atInfo().log("does it fit?")
        return true
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Recipes.WandSerializer
    }
    companion object {}
}