package kit.nova_arcana

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.*
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

class MatCrafting(id: Identifier,
                  group: String,
                  category: CraftingRecipeCategory,
                  width: Int,
                  height: Int,
                  val input: DefaultedList<Ingredient>,
                  output: String,
                  val showNotification: Boolean):
    ShapedRecipe(id, group, category, width, height, input, mkMateria(output), showNotification) {

    override fun getSerializer(): RecipeSerializer<*> {
        TODO()
        //return Recipes.MatSerializer
    }
    companion object Ser: RecipeSerializer<MatCrafting> {
        val logger = LoggerFactory.getLogger("nova_arcana")
        private fun findFirstSymbol(line: String): Int {
            var i = 0

            while (i < line.length && line[i] == ' ') {
                i++
            }

            return i
        }

        private fun findLastSymbol(pattern: String): Int {
            var i = pattern.length - 1

            while (i >= 0 && pattern[i] == ' ') {
                i--
            }

            return i
        }
        private fun readSymbols(json: JsonObject): Map<String, Ingredient> {
            val map: MutableMap<String, Ingredient> = Maps.newHashMap()
            for ((key, value) in json.entrySet()) {
                if (key.length != 1) {
                    throw JsonSyntaxException("Invalid key entry: '" + key as String + "' is an invalid symbol (must be 1 character only).")
                }

                if (" " == key) {
                    throw JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.")
                }

                map[key as String] = Ingredient.fromJson(value as JsonElement, false)
            }

            map[" "] = Ingredient.EMPTY
            return map
        }
        private fun removePadding(vararg pattern: List<String>): List<String> {
            var i = Int.MAX_VALUE
            var j = 0
            var k = 0
            var l = 0

            for ((m, string) in pattern[0].withIndex()) {
                i = min(i.toDouble(), findFirstSymbol(string).toDouble()).toInt()
                val n = findLastSymbol(string)
                j = max(j.toDouble(), n.toDouble()).toInt()
                if (n < 0) {
                    if (k == m) {
                        k++
                    }

                    l++
                } else {
                    l = 0
                }
            }

            if (pattern.size == l) {
                return listOf()
            } else {
                val strings = arrayOfNulls<String>(pattern.size - l - k)

                for (o in strings.indices) {
                    strings[o] = pattern[0][o + k].substring(i, j + 1)
                }

                return strings.filterNotNull()
            }
        }
        private fun getPattern(json: JsonArray): List<String> {
            val strings = arrayOfNulls<String>(json.size())
            if (strings.size > 3) {
                throw JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum")
            } else if (strings.isEmpty()) {
                throw JsonSyntaxException("Invalid pattern: empty pattern not allowed")
            } else {
                for (i in strings.indices) {
                    val string = JsonHelper.asString(json[i], "pattern[$i]")
                    if (string.length > 3) {
                        throw JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum")
                    }

                    if (i > 0 && strings[0]!!.length != string.length) {
                        throw JsonSyntaxException("Invalid pattern: each row must be the same width")
                    }

                    strings[i] = string
                }

                return strings.filterNotNull()
            }
        }
        private fun createPatternMatrix(
            pattern: List<String>,
            symbols: Map<String, Ingredient?>,
            width: Int,
            height: Int
        ): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.ofSize(width * height, Ingredient.EMPTY)
            val set: MutableSet<String> = Sets.newHashSet(symbols.keys)
            set.remove(" ")

            for (i in pattern.indices) {
                for (j in 0 until pattern[i].length) {
                    val string = pattern[i].substring(j, j + 1)
                    val ingredient = symbols[string]
                        ?: throw JsonSyntaxException("Pattern references symbol '$string' but it's not defined in the key")

                    set.remove(string)
                    defaultedList[j + width * i] = ingredient
                }
            }

            if (set.isNotEmpty()) {
                throw JsonSyntaxException("Key defines symbols that aren't used in pattern: $set")
            } else {
                return defaultedList
            }
        }
        private fun outputFromJson(json: JsonObject): String {
            val item = JsonHelper.getString(json, "item")
            if (json.has("data")) {
                throw JsonParseException("Disallowed data tag found")
            } else {
                val i = JsonHelper.getInt(json, "count", 1)
                if (i < 1) {
                    throw JsonSyntaxException("Invalid output count: $i")
                } else {
                    return item
                }
            }
        }
        override fun read(identifier: Identifier, jsonObject: JsonObject): MatCrafting {
            logger.atInfo().log("reading json")
            val string = JsonHelper.getString(jsonObject, "group", "")
            val craftingRecipeCategory = CraftingRecipeCategory.CODEC
                .byId(
                    JsonHelper.getString(jsonObject, "category", null),
                    CraftingRecipeCategory.MISC
                ) as CraftingRecipeCategory
            val map = readSymbols(JsonHelper.getObject(jsonObject, "key"))
            val strings = removePadding(getPattern(JsonHelper.getArray(jsonObject, "pattern")))
            val i = strings[0].length
            val j = strings.size
            val defaultedList = createPatternMatrix(strings, map, i, j)
            val itemStack = outputFromJson(JsonHelper.getObject(jsonObject, "result"))
            val bl = JsonHelper.getBoolean(jsonObject, "show_notification", true)
            return MatCrafting(identifier, string!!, craftingRecipeCategory, i, j, defaultedList, itemStack, bl)
        }

        override fun read(identifier: Identifier, packetByteBuf: PacketByteBuf): MatCrafting {
            val i = packetByteBuf.readVarInt()
            val j = packetByteBuf.readVarInt()
            val string = packetByteBuf.readString()
            val craftingRecipeCategory = packetByteBuf.readEnumConstant(
                CraftingRecipeCategory::class.java
            )
            val defaultedList = DefaultedList.ofSize(i * j, Ingredient.EMPTY)

            for (k in defaultedList.indices) {
                defaultedList[k] = Ingredient.fromPacket(packetByteBuf)
            }

            val itemStack = packetByteBuf.readString()
            val bl = packetByteBuf.readBoolean()
            return MatCrafting(identifier, string, craftingRecipeCategory, i, j, defaultedList, itemStack, bl)
        }

        override fun write(packetByteBuf: PacketByteBuf, shapedRecipe: MatCrafting) {
            packetByteBuf.writeVarInt(shapedRecipe.width)
            packetByteBuf.writeVarInt(shapedRecipe.height)
            packetByteBuf.writeString(shapedRecipe.group)
            packetByteBuf.writeEnumConstant(shapedRecipe.category)

            for (ingredient in shapedRecipe.input) {
                ingredient.write(packetByteBuf)
            }

            packetByteBuf.writeItemStack(shapedRecipe.output)
            packetByteBuf.writeBoolean(shapedRecipe.showNotification)
        }
    }
}