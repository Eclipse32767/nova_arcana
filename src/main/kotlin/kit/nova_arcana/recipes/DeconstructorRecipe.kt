package kit.nova_arcana.recipes

import com.google.gson.Gson
import com.google.gson.JsonObject
import kit.nova_arcana.blocks.ManaFilter
import kit.nova_arcana.blocks.PedestalEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.world.World

class ManaOutputs(val fire: Int, val ice: Int, val earth: Int, val wind: Int, val spirit: Int, val nil: Int) {
    fun writeBuf(buf: PacketByteBuf) {
        buf.writeInt(fire)
        buf.writeInt(ice)
        buf.writeInt(earth)
        buf.writeInt(wind)
        buf.writeInt(spirit)
        buf.writeInt(nil)
    }
    companion object {
        fun readBuf(buf: PacketByteBuf): ManaOutputs {
            return ManaOutputs(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt())
        }
    }
    fun pairList(): Array<Pair<ManaFilter, Int>> {
        return arrayOf(Pair(ManaFilter.FIRE, fire), Pair(ManaFilter.ICE, ice), Pair(ManaFilter.EARTH, earth), Pair(ManaFilter.WIND, wind), Pair(ManaFilter.SPIRIT, spirit), Pair(ManaFilter.VOID, nil))
    }
    fun withSet(filter: ManaFilter, v: Int): ManaOutputs {
        return when (filter) {
            ManaFilter.FIRE -> ManaOutputs(v, ice, earth, wind, spirit, nil)
            ManaFilter.ICE -> ManaOutputs(fire, v, earth, wind, spirit, nil)
            ManaFilter.WIND -> ManaOutputs(fire, ice, earth, v, spirit, nil)
            ManaFilter.EARTH -> ManaOutputs(fire, ice, v, wind, spirit, nil)
            ManaFilter.SPIRIT -> ManaOutputs(fire, ice, earth, wind, v, nil)
            ManaFilter.VOID -> ManaOutputs(fire, ice, earth, wind, spirit, v)
        }
    }
}

class DeconstructorRecipe(val input: Ingredient, val output: ManaOutputs): Recipe<PedestalEntity> {
    override fun matches(inventory: PedestalEntity, world: World): Boolean {
        return input.test(inventory.inv)
    }

    override fun craft(inventory: PedestalEntity?, registryManager: DynamicRegistryManager?): ItemStack {
        return ItemStack.EMPTY
    }

    override fun fits(width: Int, height: Int): Boolean {
        return true
    }

    override fun getOutput(registryManager: DynamicRegistryManager?): ItemStack {
        return ItemStack.EMPTY
    }

    override fun getId(): Identifier {
        return Identifier("nova_arcana:deconstructor_crafting")
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return DeconstructorRecipeSer
    }

    override fun getType(): RecipeType<*> {
        return DeconstructorRecipeType
    }
}
object DeconstructorRecipeType: RecipeType<DeconstructorRecipe>
object DeconstructorRecipeSer: RecipeSerializer<DeconstructorRecipe> {
    class JsonFormat(
        val ingredient: JsonObject,
        val output: JsonObject
    )
    override fun read(id: Identifier, json: JsonObject): DeconstructorRecipe {
        val fmt: JsonFormat = Gson().fromJson(json, JsonFormat::class.java)
        val input = Ingredient.fromJson(fmt.ingredient)
        val output: ManaOutputs = Gson().fromJson(fmt.output, ManaOutputs::class.java)
        return DeconstructorRecipe(input, output)
    }

    override fun read(id: Identifier, buf: PacketByteBuf): DeconstructorRecipe {
        return DeconstructorRecipe(Ingredient.fromPacket(buf), ManaOutputs.readBuf(buf))
    }

    override fun write(buf: PacketByteBuf, recipe: DeconstructorRecipe) {
        recipe.input.write(buf)
        recipe.output.writeBuf(buf)
    }
}