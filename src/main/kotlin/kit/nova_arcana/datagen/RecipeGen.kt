package kit.nova_arcana.datagen

import kit.nova_arcana.*
import kit.nova_arcana.blocks.ManaFilter
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.block.Blocks
import net.minecraft.data.DataOutput
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.ItemConvertible
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import java.util.function.Consumer

class RecipeGen(output: DataOutput) : FabricRecipeProvider(output as FabricDataOutput?) {
    private fun mkShaped(category: RecipeCategory, item: ItemConvertible, exp: Consumer<RecipeJsonProvider>, fn: (ShapedRecipeJsonBuilder) -> ShapedRecipeJsonBuilder) {
        fn(ShapedRecipeJsonBuilder.create(category, item)).offerTo(exp)
    }
    private fun mkShaped(category: RecipeCategory, item: ItemConvertible, count: Int, exp: Consumer<RecipeJsonProvider>, fn: (ShapedRecipeJsonBuilder) -> ShapedRecipeJsonBuilder) {
        fn(ShapedRecipeJsonBuilder.create(category, item, count)).offerTo(exp)
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
            .inputAuto('A', Items.HONEY_BOTTLE)
        }
        mkShaped(RecipeCategory.MISC, ModBlocks.PEDESTAL, 4, exp) {
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
            it.addInput(3, ModItems.alchRuby)
            it.addInput(1, ModItems.amber)
            it.name = "nova_arcana:infusion/ritual_forge"
            it.setCentral(Blocks.STONE_BRICKS)
            it.setOutItm(ModBlocks.RITUAL_FORGE)
            it.setCost(ManaFilter.FIRE, 30)
            it.setCost(ManaFilter.WIND, 10)
        }
        mkInfusion(exp) {
            it.addInputs(ModItems.alchRuby)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(ModItems.alchRuby)
            it.setOutCount(2)
            it.setCost(ManaFilter.FIRE, 20)
        }
        mkInfusion(exp) {
            it.addInputs(Items.PRISMARINE_SHARD)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(Items.PRISMARINE_SHARD)
            it.setOutCount(2)
            it.setCost(ManaFilter.ICE, 20)
        }
        mkInfusion(exp) {
            it.addInputs(Items.EMERALD)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(Items.EMERALD)
            it.setOutCount(2)
            it.setCost(ManaFilter.EARTH, 20)
        }
        mkInfusion(exp) {
            it.addInputs(ModItems.amber)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(ModItems.amber)
            it.setOutCount(2)
            it.setCost(ManaFilter.WIND, 20)
        }
        mkInfusion(exp) {
            it.addInputs(Items.QUARTZ)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(Items.QUARTZ)
            it.setOutCount(2)
            it.setCost(ManaFilter.SPIRIT, 20)
        }
        mkInfusion(exp) {
            it.addInputs(Items.AMETHYST_SHARD)
            it.setCentral(Items.LAPIS_LAZULI)
            it.setOutItm(Items.AMETHYST_SHARD)
            it.setOutCount(2)
            it.setCost(ManaFilter.VOID, 20)
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