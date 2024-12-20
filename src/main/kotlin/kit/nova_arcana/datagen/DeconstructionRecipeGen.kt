package kit.nova_arcana.datagen

import com.google.gson.Gson
import com.google.gson.JsonObject
import kit.nova_arcana.Recipes
import kit.nova_arcana.blocks.ManaFilter
import kit.nova_arcana.recipes.ManaOutputs
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.ItemConvertible
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class DeconstructionBuilder {
    var input: Ingredient? = null
    var outputs: ManaOutputs = ManaOutputs(0, 0, 0, 0, 0, 0)
    var id: String = ""
    fun setOutput(f: ManaFilter, v: Int) {
        outputs = outputs.withSet(f, v)
    }
    fun setAllOutput(v: Int) {
        outputs = ManaOutputs(v, v, v, v, v, v)
    }
    fun setInput(v: ItemConvertible) {
        id = Registries.ITEM.getId(v.asItem()).path
        input = Ingredient.ofItems(v)
    }
    fun build(): DeconstructionMaker {
        return DeconstructionMaker(input!!, outputs, id)
    }
}

class DeconstructionMaker(val input: Ingredient, val outputs: ManaOutputs, val id: String): RecipeJsonProvider {
    override fun serialize(json: JsonObject) {
        json.add("ingredient", input.toJson())
        json.add("output", Gson().toJsonTree(outputs, ManaOutputs::class.java))
    }
    override fun getRecipeId(): Identifier {
        return Identifier("nova_arcana", "deconstruction/$id")
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Recipes.DeconstructorSerializer
    }

    override fun toAdvancementJson(): JsonObject {
        val h: Advancement.Builder = Advancement.Builder.createUntelemetered().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
        return h.toJson()
    }

    override fun getAdvancementId(): Identifier {
        val id = recipeId
        return Identifier(id.namespace, "recipes/" + id.path)
    }
}