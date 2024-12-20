package kit.nova_arcana.datagen

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kit.nova_arcana.Recipes
import kit.nova_arcana.blocks.ManaFilter
import kit.nova_arcana.recipes.ManaOutputs
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement.*
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class InfusionBuilder {
    private var central: Ingredient? = null
    private val inputs: MutableList<Ingredient> = mutableListOf()
    private var out: ItemStack = ItemStack.EMPTY
    private var manaCost: ManaOutputs = ManaOutputs(0, 0, 0, 0, 0, 0)
    var name: String? = null
    fun addInputs(vararg v: Ingredient) {
        for (i in v) inputs += i
    }
    fun addInputs(vararg v: ItemConvertible) {
        for (i in v) inputs += Ingredient.ofItems(i)
    }
    fun addInput(times: Int, v: Ingredient) {
        for (i in 1..times) addInputs(v)
    }
    fun addInput(times: Int, v: ItemConvertible) {
        for (i in 1..times) addInputs(v)
    }
    fun setCost(type: ManaFilter, v: Int) {
        manaCost = manaCost.withSet(type, v)
    }
    fun setAllCost(v: Int) {
        manaCost = ManaOutputs(v, v, v, v, v, v)
    }
    fun setOutItm(item: ItemConvertible) {
        out = item.asItem().defaultStack
    }
    fun setOutCount(count: Int) {
        out.count = count
    }
    fun setOutStk(item: ItemStack) {
        out = item
    }
    fun alterOutNbt(fn: (NbtCompound) -> Unit) {
        fn(out.orCreateNbt)
    }
    fun setCentral(v: Ingredient) {
        central = v
    }
    fun setCentral(v: ItemConvertible) {
        central = Ingredient.ofItems(v)
    }
    fun build(): InfusionMaker {
        val id = Registries.ITEM.getId(out.item)
        val fallback = Identifier(id.namespace, "infusion/"+id.path)
        return InfusionMaker(central!!, inputs, out, manaCost, if (name == null) fallback else Identifier(name))
    }
}

class InfusionMaker(val central: Ingredient, val inputs: List<Ingredient>, val out: ItemStack, val manaCost: ManaOutputs, val name: Identifier):
    RecipeJsonProvider {
    override fun serialize(json: JsonObject) {
        //if you feed me any of these inputs, go fuck yourself
        if (inputs.isEmpty()) throw IllegalStateException()
        if (out.isEmpty) throw IllegalStateException()
        val inputArr = JsonArray()
        for (input in inputs) {
            inputArr.add(input.toJson())
        }
        json.add("inputs", inputArr)
        json.add("central", central.toJson())
        json.addProperty("out", Registries.ITEM.getId(out.item).toString())
        json.addProperty("outCount", out.count)
        val nbtObj = JsonObject()
        for (entry in out.orCreateNbt.entries) {
            val t = entry.value.type
            if (t == INT_TYPE) nbtObj.addProperty(entry.key, out.orCreateNbt.getInt(entry.key))
            if (t == DOUBLE_TYPE) nbtObj.addProperty(entry.key, out.orCreateNbt.getDouble(entry.key))
            if (t == STRING_TYPE) nbtObj.addProperty(entry.key, out.orCreateNbt.getString(entry.key))
        }
        json.add("outData", nbtObj)
        json.add("manaInputs", Gson().toJsonTree(manaCost, ManaOutputs::class.java))
    }

    override fun getRecipeId(): Identifier {
        return name
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Recipes.InfusionSerializer
    }

    override fun toAdvancementJson(): JsonObject {
        val h: Advancement.Builder = Advancement.Builder.createUntelemetered().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
        return h.toJson()
    }

    override fun getAdvancementId(): Identifier {
        return Identifier(name.namespace, "recipes/${name.path}")
    }
}