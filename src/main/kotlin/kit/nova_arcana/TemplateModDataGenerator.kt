package kit.nova_arcana

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kit.nova_arcana.blocks.ManaFilter
import kit.nova_arcana.recipes.ManaOutputs
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.block.Blocks
import net.minecraft.data.DataOutput
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement.*
import net.minecraft.nbt.NbtType
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer

object TemplateModDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
		val pack = gen.createPack()
		pack.addProvider { v -> RecipeGen(v) }
	}
}

class RecipeGen(output: DataOutput) : FabricRecipeProvider(output as FabricDataOutput?) {
	private fun mkShaped(category: RecipeCategory, item: ItemConvertible, exp: Consumer<RecipeJsonProvider>, fn: (ShapedRecipeJsonBuilder) -> ShapedRecipeJsonBuilder) {
		fn(ShapedRecipeJsonBuilder.create(category, item)).offerTo(exp)
	}
	private fun mkInfusion(exp: Consumer<RecipeJsonProvider>, fn: (InfusionBuilder) -> Unit) {
		val builder = InfusionBuilder()
		fn(builder)
		exp.accept(builder.build())
	}
	private fun mkDeconstruction(exp: Consumer<RecipeJsonProvider>, fn: (DeconstructionBuilder) -> Unit) {
		val builder = DeconstructionBuilder()
		fn(builder)
		exp.accept(builder.build())
	}
	override fun generate(exp: Consumer<RecipeJsonProvider>) {
		mkShaped(RecipeCategory.MISC, ModItems.alchRuby, exp) {it
			.pattern(" A ")
			.pattern("AAA")
			.pattern(" B ")
			.inputAuto('A', Items.BLAZE_POWDER)
			.inputAuto('B', Blocks.NETHERRACK)
		}
		mkShaped(RecipeCategory.MISC, ModItems.amber, exp) {it
			.pattern(" A ")
			.pattern("AAA")
			.pattern(" A ")
			.inputAuto('A', Items.FEATHER)
		}
		mkInfusion(exp) {
			it.addInput(8, Items.GOLD_BLOCK)
			it.setCentral(Items.GOLDEN_APPLE)
			it.setOutItm(Items.ENCHANTED_GOLDEN_APPLE)
			it.setAllCost(40)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.EMERALD)
			it.setOutput(ManaFilter.EARTH, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.AMETHYST_SHARD)
			it.setOutput(ManaFilter.VOID, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.QUARTZ)
			it.setOutput(ManaFilter.SPIRIT, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.PRISMARINE_SHARD)
			it.setOutput(ManaFilter.ICE, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(ModItems.amber)
			it.setOutput(ManaFilter.WIND, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(ModItems.alchRuby)
			it.setOutput(ManaFilter.FIRE, 20)
		}
	}
}

private fun ShapedRecipeJsonBuilder.inputAuto(c: Char, input: ItemConvertible): ShapedRecipeJsonBuilder {
	return this.input(c, input).criterion(FabricRecipeProvider.hasItem(input), FabricRecipeProvider.conditionsFromItem(input))
}

class InfusionBuilder {
	private var central: Ingredient? = null
	private val inputs: MutableList<Ingredient> = mutableListOf()
	private var out: ItemStack = ItemStack.EMPTY
	private var manaCost: ManaOutputs = ManaOutputs(0, 0, 0, 0, 0, 0)
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
		return InfusionMaker(central!!, inputs, out, manaCost)
	}
}

class InfusionMaker(val central: Ingredient, val inputs: List<Ingredient>, val out: ItemStack, val manaCost: ManaOutputs): RecipeJsonProvider {
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
		val id = Registries.ITEM.getId(out.item)
		return Identifier(id.namespace, "infusion/"+id.path)
	}

	override fun getSerializer(): RecipeSerializer<*> {
		return Recipes.InfusionSerializer
	}

	override fun toAdvancementJson(): JsonObject {
		return JsonObject()
	}

	override fun getAdvancementId(): Identifier {
		return Identifier("")
	}
}

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
		return JsonObject()
	}

	override fun getAdvancementId(): Identifier {
		return Identifier("")
	}
}