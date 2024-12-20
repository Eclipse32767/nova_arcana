package kit.nova_arcana

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import kit.nova_arcana.recipes.InfusionRecipe
import kit.nova_arcana.recipes.InfusionType
import net.minecraft.block.Blocks
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt
import kotlin.math.truncate

object NovaEmiPlugin: EmiPlugin {
    val INFUSION_WORKSTATION = EmiStack.of(ModBlocks.INFUSION_STONE)
    val INFUSION_CATEGORY = EmiRecipeCategory(Identifier("nova_arcana:infusion"), INFUSION_WORKSTATION)
    val logger = LoggerFactory.getLogger("emi-plugin")
    override fun register(reg: EmiRegistry) {
        reg.addCategory(INFUSION_CATEGORY)
        reg.addWorkstation(INFUSION_CATEGORY, INFUSION_WORKSTATION)
        val manager = reg.recipeManager
        Blocks.TORCH
        for (recipe in manager.listAllOfType(InfusionType)) {
            reg.addRecipe(InfusionEmiRecipe(recipe))
        }
    }
}

class InfusionEmiRecipe(src: InfusionRecipe): EmiRecipe {
    private val id = src.id
    private val out = EmiStack.of(src.output)
    private val central = EmiIngredient.of(src.central)
    private val outer = src.inputs.map { EmiIngredient.of(it) }
    private val mana = src.manaIn

    override fun getCategory() = NovaEmiPlugin.INFUSION_CATEGORY

    override fun getId() = id

    override fun getInputs(): MutableList<EmiIngredient> {
        return (outer+central).toMutableList()
    }

    override fun getOutputs(): MutableList<EmiStack> {
        return mutableListOf(out)
    }

    override fun getDisplayWidth(): Int {
        return 76
    }

    override fun getDisplayHeight(): Int {
        return 18*((outer.size/2)+1)
    }

    override fun addWidgets(widgets: WidgetHolder) {
        widgets.addSlot(central, 0, 0).recipeContext(this)
        val manaCost = mutableListOf<TooltipComponent>()
        if (mana.fire != 0) manaCost += TooltipComponent.of(Text.literal("Fire: ${mana.fire}").asOrderedText())
        if (mana.ice != 0) manaCost += TooltipComponent.of(Text.literal("Ice: ${mana.ice}").asOrderedText())
        if (mana.earth != 0) manaCost += TooltipComponent.of(Text.literal("Earth: ${mana.earth}").asOrderedText())
        if (mana.wind != 0) manaCost += TooltipComponent.of(Text.literal("Wind: ${mana.wind}").asOrderedText())
        if (mana.spirit != 0) manaCost += TooltipComponent.of(Text.literal("Spirit: ${mana.spirit}").asOrderedText())
        if (mana.nil != 0) manaCost += TooltipComponent.of(Text.literal("Void: ${mana.nil}").asOrderedText())
        widgets.addTexture(EmiTexture.FULL_ARROW, 26, 1).tooltip(
            manaCost
        )
        for ((i, reagent) in outer.withIndex()) {
            val offset = if (i%2 == 1) 0 else 18
            val vOffset = truncate(i/2.0)
            widgets.addSlot(reagent, offset, 18*(vOffset.toInt()+1))
        }
        widgets.addSlot(out, 58, 0).recipeContext(this)
    }

}