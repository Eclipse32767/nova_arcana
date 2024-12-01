package kit.nova_arcana.recipes

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kit.nova_arcana.blocks.InfusionPedestals
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class InfusionRecipe(val output: ItemStack, val central: Ingredient, val inputs: List<Ingredient>): Recipe<InfusionPedestals> {

    override fun matches(inventory: InfusionPedestals, world: World): Boolean {
        val logger = LoggerFactory.getLogger("pedestal_crafting")
        if (world.isClient) return false
        if (!central.test(inventory.central.inv)) return false
        class Count(var i: Ingredient, var c: Int) {
            operator fun plus(n: Int): Count {
                return Count(i, c+n)
            }
        }
        val nums = HashMap<String, Count>()
        for (ingredient in inputs) {
            val str = ingredient.toJson().toString()
            val count = nums.getOrDefault(str, Count(ingredient, 0))
            nums[str] = count + 1
            //logger.atInfo().log( "${ingredient.toJson().toString()}, ${nums[ingredient]}")
        }
        for (ingredient in nums) {
            logger.atInfo().log("${ingredient.key}, ${ingredient.value.c}")
            var count = 0
            for ((i, stack) in inventory.items.withIndex()) {
                if (ingredient.value.i.test(stack)) count++
            }
            if (count < ingredient.value.c) return false
        }
        return true
    }

    override fun craft(inventory: InfusionPedestals, registryManager: DynamicRegistryManager): ItemStack {
        return output
    }

    override fun fits(width: Int, height: Int): Boolean {
        return true
    }

    override fun getOutput(registryManager: DynamicRegistryManager): ItemStack {
        return output
    }

    override fun getId(): Identifier {
        return Identifier("nova_arcana:fusion_crafting")
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return InfusionSer
    }

    override fun getType(): RecipeType<*> {
        return InfusionType
    }
}

object InfusionType: RecipeType<InfusionRecipe>
object InfusionSer: RecipeSerializer<InfusionRecipe> {
    class JsonFormat(
        val inputs: MutableList<JsonObject>,
        val central: JsonObject,
        val out: String,
        val outCount: Int,
        val outData: JsonObject
    )
    override fun read(id: Identifier, json: JsonObject): InfusionRecipe {
        val fmt: JsonFormat = Gson().fromJson(json, JsonFormat::class.java)
        val ingredients: MutableList<Ingredient> = fmt.inputs.map { Ingredient.fromJson(it) } as MutableList<Ingredient>
        val central = Ingredient.fromJson(fmt.central)
        val outItm = Registries.ITEM.getOrEmpty(Identifier(fmt.out)).get()
        val outStk = ItemStack(outItm, fmt.outCount)
        for (v in fmt.outData.asMap()) {
            try {
                outStk.orCreateNbt.putInt(v.key, v.value.asInt)
                continue
            } catch (_: Exception) {}
            try {
                outStk.orCreateNbt.putDouble(v.key, v.value.asDouble)
                continue
            } catch (_: Exception) {}
            try {
                outStk.orCreateNbt.putString(v.key, v.value.asString)
                continue
            } catch (_: Exception) {}
        }
        return InfusionRecipe(outStk, central, ingredients)
    }
    override fun read(id: Identifier, buf: PacketByteBuf): InfusionRecipe {
        val output = buf.readItemStack()
        val central = Ingredient.fromPacket(buf)
        val terminator = buf.readInt()
        val inputs = mutableListOf<Ingredient>()
        for (i in 0..terminator) {
            inputs += Ingredient.fromPacket(buf)
        }
        return InfusionRecipe(output, central, inputs)
    }

    override fun write(buf: PacketByteBuf, recipe: InfusionRecipe) {
        buf.writeItemStack(recipe.output)
        recipe.central.write(buf)
        buf.writeInt(recipe.inputs.lastIndex)
        for (item in recipe.inputs) {
            item.write(buf)
        }
    }
}