package kit.nova_arcana

import kit.nova_arcana.recipes.*
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object Recipes {
    val WandSerializer = RecipeSerializer.register("nova_arcana:crafting_special_wand", SpecialRecipeSerializer { a, b ->
        WandCrafting(
            a,
            b,
        )
    })
    //val regWandSer = Registry.register(Registries.RECIPE_SERIALIZER, "nova_arcana:crafting_special_wand", WandSerializer)
    val WAND_CRAFTING = Registry.register(Registries.RECIPE_TYPE, "nova_arcana:crafting_special_wand", object : RecipeType<WandCrafting> {})
    //val MatSerializer = RecipeSerializer.register("nova_arcana:crafting_mat_shaped", MatCrafting.Ser)
    //val matRecipe = Registry.register(Registries.RECIPE_TYPE, "nova_arcana:crafting_mat_shaped", object : RecipeType<MatCrafting> {})
    val InfusionSerializer = RecipeSerializer.register("nova_arcana:infusion", InfusionSer)
    val INFUSION = Registry.register(Registries.RECIPE_TYPE, Identifier("nova_arcana:infusion"), InfusionType)
    val DeconstructorSerializer = RecipeSerializer.register("nova_arcana:deconstruction", DeconstructorRecipeSer)
    val DECONSTRUCTION = Registry.register(Registries.RECIPE_TYPE, Identifier("nova_arcana:deconstruction"), DeconstructorRecipeType)
}