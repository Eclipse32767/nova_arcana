package kit.nova_arcana

import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object Recipes {
    val WandSerializer = RecipeSerializer.register("nova_arcana:crafting_special_wand", SpecialRecipeSerializer { a, b ->
        WandCrafting(
            a,
            b,
        )
    })
    //val regWandSer = Registry.register(Registries.RECIPE_SERIALIZER, "nova_arcana:crafting_special_wand", WandSerializer)
    val wandRecipe = Registry.register(Registries.RECIPE_TYPE, "nova_arcana:crafting_special_wand", object : RecipeType<WandCrafting> {})
    //val MatSerializer = RecipeSerializer.register("nova_arcana:crafting_mat_shaped", MatCrafting.Ser)
    //val matRecipe = Registry.register(Registries.RECIPE_TYPE, "nova_arcana:crafting_mat_shaped", object : RecipeType<MatCrafting> {})
}