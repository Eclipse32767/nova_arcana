package kit.nova_arcana.items

import kit.nova_arcana.*
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import org.slf4j.LoggerFactory

fun mkRank(num: Int): WandRank {
    return WandRank.entries.filter { rank -> rank.num == num }[0]
}

enum class WandRank(val num: Int) {
    CHEATER(-1),
    FIREONLY(0),
    FROSTONLY(1),
    EARTHONLY(2),
    AIRONLY(3),
    VOIDONLY(4),
    SPIRITONLY(5),
    PRIMAL(6),
    TIER1(11),
    TIER2(21),
    TIER3(31);

    fun canCast(req: WandRank): Boolean {
        if (this == CHEATER) return true
        if (req.num == num) return true
        if (num > 5 && num >= req.num) return true
        return false
    }
}

/*
Wand NBT Tags
core, decor, gem: visual
avail_spells: newline separated spell list
slots_s: single slots
slots_f: fused slots
mods: spell modifiers
name: item name
tier: a valid WandRank, used to determine if a wand can cast a specific spell
 */
class WandItem(s: FabricItemSettings): Item(s) {
    private val logger = LoggerFactory.getLogger("nova_arcana")
    fun spellPick(stk: ItemStack): Identifier? {
        var out: Identifier? = null
        try {
            val nbt = stk.orCreateNbt
            val spells = spellList(stk)
            val pickedSpell = nbt.getInt("spell")
            out = Identifier(spells[pickedSpell])
        } catch (_: IndexOutOfBoundsException) {}
        return out
    }
    fun spellMax(stk: ItemStack): Int {
        return spellList(stk).lastIndex
    }
    fun spellList(stk: ItemStack): List<String> {
        val nbt = stk.orCreateNbt
        val rawspells = nbt.getString("avail_spells")
        //if (rawspells == "") {
        //    rawspells = "nova_arcana:spell/flame\nnova_arcana:spell/siphon\nnova_arcana:spell/excavate"
        //    nbt.putString("avail_spells", rawspells)
        //}
        val spells = rawspells.split('\n')
        return spells
    }
    fun getMod(stk: ItemStack): SpellMod {
        return try {
            mkMod(stk.orCreateNbt.getIntArray("mods")[stk.orCreateNbt.getInt("spell")])
        } catch (_: IndexOutOfBoundsException) {
            SpellMod.NONE
        }
    }


    override fun getName(stack: ItemStack): Text {
        return Text.literal(stack.orCreateNbt.getString("name"))
    }
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        if (tooltip != null && stack != null) {
            val nbt = stack.orCreateNbt
            val slots = (nbt?.getInt("slots_f") ?: 0) * 2 + (nbt?.getInt("slots_s") ?: 0)
            val spells = spellList(stack)
            val mods = nbt.getIntArray("mods")
            var spellcount = if (spells.isEmpty() || spells[0].isEmpty()) 0 else spells.lastIndex.plus(1)
            spellcount += if (mods.isEmpty()) 0 else mods.lastIndex.plus(1)
            //logger.atInfo().log(spellcount.toString())
            tooltip += Text.literal("Materia Slots: $slots, ($spellcount filled)")
            var curMod = 0

            val spellStr = spells.map { run {
                val spellname = spellReg[Identifier(it)]?.name?.string ?: ""
                val mod = try {mods?.get(curMod)} catch (_: IndexOutOfBoundsException) {null}
                curMod++
                val modname = if (mod == null) "" else " (${mkMod(mod).pretty_name()})"
                spellname+modname
            }}
            tooltip += Text.literal("Spells: ")
            for (spell in spellStr) {
                if (spell.isNotEmpty()) tooltip += Text.literal(spell)
            }
        }
    }
    override fun inventoryTick(stk: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stk, world, entity, slot, selected)
        if (selected) {
            val nbt = stk.orCreateNbt
            val spell = nbt.getInt("spell")
            val castable = spellInvReg[spellPick(stk)]
            castable?.hover(stk, world, entity, slot, selected, getMod(stk))
        }
    }
    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack>? {
        if (world == null || hand == null || user == null) {
            return null
        }

        val stk = user.getStackInHand(hand)

        val castable = spellPick(stk)
        val spellres: SpellCastResult? = spellReg[castable]?.cast(world, user, hand, getMod(stk))
        if (spellres == SpellCastResult.FAIL) {
            return TypedActionResult.pass(stk)
        }

        val h = ManaHandle(user)
        h.castTimer = 100
        h.syncMana()
        return TypedActionResult.success(stk)
    }
}