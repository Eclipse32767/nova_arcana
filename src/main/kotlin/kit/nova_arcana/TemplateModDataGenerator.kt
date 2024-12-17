package kit.nova_arcana

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.fabricators_of_create.porting_lib.tags.Tags
import kit.nova_arcana.blocks.ManaFilter
import kit.nova_arcana.recipes.ManaOutputs
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.block.Blocks
import net.minecraft.data.DataOutput
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.Model
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
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
import team.lodestar.lodestone.systems.datagen.ItemModelSmithTypes
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
	private fun mkShapeless(category: RecipeCategory, item: ItemConvertible, exp: Consumer<RecipeJsonProvider>, fn: (ShapelessRecipeJsonBuilder) -> ShapelessRecipeJsonBuilder) {
		fn(ShapelessRecipeJsonBuilder.create(category, item)).offerTo(exp)
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
			.pattern("ACA")
			.pattern(" B ")
			.inputAuto('A', Items.COAL)
			.inputAuto('B', Blocks.NETHERRACK)
			.inputAuto('C', Items.COAL_BLOCK)
		}
		mkShaped(RecipeCategory.MISC, ModItems.amber, exp) {it
			.pattern(" A ")
			.pattern("AAA")
			.pattern(" A ")
			.inputAuto('A', Items.FEATHER)
		}
		mkShaped(RecipeCategory.MISC, ModBlocks.PEDESTAL, exp) {
			it.pattern(" A ")
				.pattern("AAA")
				.pattern("AAA")
				.inputAuto('A', Items.STONE)
		}
		mkShaped(RecipeCategory.MISC, ModBlocks.INFUSION_STONE, exp) {
			it.pattern("AAA")
				.pattern("ABA")
				.pattern("AAA")
				.inputAuto('A', Items.GLASS_PANE)
				.inputAuto('B', Items.CLAY_BALL)
		}
		mkShaped(RecipeCategory.MISC, ModBlocks.DECONSTRUCTOR, exp) {
			it.pattern("AAA")
				.pattern("BAB")
				.pattern("AAA")
				.inputAuto('A', Items.STONE)
				.inputAuto('B', Items.GLASS)
		}
		for (p in listOf(
			Pair(ModItems.alchRuby, ModBlocks.MANA_VESSEL_FIRE),
			Pair(Items.PRISMARINE_SHARD, ModBlocks.MANA_VESSEL_ICE),
			Pair(Items.EMERALD, ModBlocks.MANA_VESSEL_EARTH),
			Pair(ModItems.amber, ModBlocks.MANA_VESSEL_WIND),
			Pair(Items.QUARTZ, ModBlocks.MANA_VESSEL_SPIRIT),
			Pair(Items.AMETHYST_SHARD, ModBlocks.MANA_VESSEL_VOID)
		)) {
			mkShaped(RecipeCategory.MISC, p.second, exp) {
				it.pattern("ABA")
					.pattern("ACA")
					.pattern("AAA")
					.inputAuto('A', Items.GLASS_PANE)
					.inputAuto('B', Items.CLAY_BALL)
					.inputAuto('C', p.first)
			}
		}
		mkShapeless(RecipeCategory.MISC, ModItems.crystalChisel, exp) {
			it.inputAuto(Items.STICK)
				.inputAuto(Items.COPPER_INGOT)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemRuby, exp) {
			it.inputAuto(ModItems.alchRuby)
				.inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemQuartz, exp) {
			it.inputAuto(Items.QUARTZ).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemAmethyst, exp) {
			it.inputAuto(Items.AMETHYST_SHARD).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemEmerald, exp) {
			it.inputAuto(Items.EMERALD).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemAmber, exp) {
			it.inputAuto(ModItems.amber).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemPrismarine, exp) {
			it.inputAuto(Items.PRISMARINE_SHARD).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemDiamond, exp) {
			it.inputAuto(Items.DIAMOND).inputAuto(ModItems.crystalChisel)
		}
		mkShapeless(RecipeCategory.MISC, ModItems.GemSurrogates.gemPristine, exp) {
			it.inputAuto(ModItems.pristineDiamond).inputAuto(ModItems.crystalChisel)
		}
		mkInfusion(exp) {
			it.addInput(8, Items.GOLD_BLOCK)
			it.setCentral(Items.GOLDEN_APPLE)
			it.setOutItm(Items.ENCHANTED_GOLDEN_APPLE)
			it.setAllCost(40)
		}
		mkInfusion(exp) {
			it.addInput(2, Items.DIAMOND)
			it.name = "nova_arcana:infusion/pristine_diamond"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutItm(ModItems.pristineDiamond)
			it.setOutCount(2)
			it.setAllCost(120)
		}
		mkInfusion(exp) {
			it.addInput(2, Items.STRIPPED_WARPED_STEM)
			it.name = "nova_arcana:infusion/warped_core"
			it.setCentral(Items.GOLD_INGOT)
			it.setOutStk(Prefabs.CORE_WARPED)
			it.setAllCost(40)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.COAL)
			it.name = "nova_arcana:infusion/flame"
			it.setCentral(ModItems.alchRuby)
			it.setOutStk(matStk("flame"))
			it.setCost(ManaFilter.FIRE, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.BONE)
			it.name = "nova_arcana:infusion/siphon"
			it.setCentral(Items.AMETHYST_SHARD)
			it.setOutStk(matStk("siphon"))
			it.setCost(ManaFilter.VOID, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.STONE_PICKAXE)
			it.name = "nova_arcana:infusion/excavate"
			it.setCentral(Items.EMERALD)
			it.setOutStk(matStk("excavate"))
			it.setCost(ManaFilter.EARTH, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.SNOWBALL)
			it.name = "nova_arcana:infusion/support"
			it.setCentral(Items.PRISMARINE_SHARD)
			it.setOutStk(matStk("support"))
			it.setCost(ManaFilter.ICE, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.SUGAR)
			it.name = "nova_arcana:infusion/dash"
			it.setCentral(ModItems.amber)
			it.setOutStk(matStk("dash"))
			it.setCost(ManaFilter.WIND, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.BONE_MEAL)
			it.name = "nova_arcana:infusion/recovery"
			it.setCentral(Items.QUARTZ)
			it.setOutStk(matStk("recovery"))
			it.setCost(ManaFilter.SPIRIT, 30)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.STONE_PICKAXE)
			it.addInput(4, Items.SNOWBALL)
			it.name = "nova_arcana:infusion/substitute"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutStk(matStk("substitute"))
			it.setCost(ManaFilter.EARTH, 60)
			it.setCost(ManaFilter.ICE, 60)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.COAL)
			it.addInput(4, Items.FEATHER)
			it.name = "nova_arcana:infusion/immolate"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutStk(matStk("immolate"))
			it.setCost(ManaFilter.FIRE, 60)
			it.setCost(ManaFilter.WIND, 60)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.ROTTEN_FLESH)
			it.addInput(2, Items.GOLDEN_SWORD)
			it.addInput(2, Items.GLISTERING_MELON_SLICE)
			it.name = "nova_arcana:infusion/malevolence"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutStk(matStk("malevolence"))
			it.setCost(ManaFilter.SPIRIT, 40)
			it.setCost(ManaFilter.VOID, 80)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.PACKED_ICE)
			it.addInput(4, Items.GLISTERING_MELON_SLICE)
			it.name = "nova_arcana:infusion/overclock"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutStk(matStk("overclock"))
			it.setCost(ManaFilter.SPIRIT, 60)
			it.setCost(ManaFilter.ICE, 60)
		}
		mkInfusion(exp) {
			it.addInput(4, Items.ENDER_PEARL)
			it.addInput(4, Items.FEATHER)
			it.name = "nova_arcana:infusion/magic-missile"
			it.setCentral(ModItems.pristineDiamond)
			it.setOutStk(matStk("magic-missile"))
			it.setCost(ManaFilter.ICE, 60)
			it.setCost(ManaFilter.WIND, 60)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.SMOOTH_STONE)
			it.setOutput(ManaFilter.EARTH, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.COPPER_INGOT)
			it.setOutput(ManaFilter.EARTH, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.EMERALD)
			it.setOutput(ManaFilter.EARTH, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.ROTTEN_FLESH)
			it.setOutput(ManaFilter.VOID, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.FERMENTED_SPIDER_EYE)
			it.setOutput(ManaFilter.VOID, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.AMETHYST_SHARD)
			it.setOutput(ManaFilter.VOID, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.BONE_MEAL)
			it.setOutput(ManaFilter.SPIRIT, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.GLISTERING_MELON_SLICE)
			it.setOutput(ManaFilter.SPIRIT, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.QUARTZ)
			it.setOutput(ManaFilter.SPIRIT, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.SNOW_BLOCK)
			it.setOutput(ManaFilter.ICE, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.PACKED_ICE)
			it.setOutput(ManaFilter.ICE, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.PRISMARINE_SHARD)
			it.setOutput(ManaFilter.ICE, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.FEATHER)
			it.setOutput(ManaFilter.WIND, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.PHANTOM_MEMBRANE)
			it.setOutput(ManaFilter.WIND, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(ModItems.amber)
			it.setOutput(ManaFilter.WIND, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.COAL)
			it.setOutput(ManaFilter.FIRE, 1)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.BLAZE_POWDER)
			it.setOutput(ManaFilter.FIRE, 5)
		}
		mkDeconstruction(exp) {
			it.setInput(ModItems.alchRuby)
			it.setOutput(ManaFilter.FIRE, 20)
		}
		mkDeconstruction(exp) {
			it.setInput(Items.DIAMOND)
			it.setAllOutput(10)
		}
		mkDeconstruction(exp) {
			it.setInput(ModItems.pristineDiamond)
			it.setAllOutput(60)
		}
	}
}

private fun ShapelessRecipeJsonBuilder.inputAuto(input: ItemConvertible): ShapelessRecipeJsonBuilder {
	return this.input(input).criterion(FabricRecipeProvider.hasItem(input), FabricRecipeProvider.conditionsFromItem(input))
}

private fun ShapedRecipeJsonBuilder.inputAuto(c: Char, input: ItemConvertible): ShapedRecipeJsonBuilder {
	return this.input(c, input).criterion(FabricRecipeProvider.hasItem(input), FabricRecipeProvider.conditionsFromItem(input))
}

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

class InfusionMaker(val central: Ingredient, val inputs: List<Ingredient>, val out: ItemStack, val manaCost: ManaOutputs, val name: Identifier): RecipeJsonProvider {
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