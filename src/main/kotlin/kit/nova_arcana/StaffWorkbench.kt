package kit.nova_arcana

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.*
import net.minecraft.screen.slot.Slot
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.commons.logging.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)

class StaffWorkbench(settings: Settings) : Block(settings) {
    val logger = LoggerFactory.getLogger("nova_arcana")
    val TITLE = Text.literal("Staff Workbench")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        //logger.atInfo().log("bing bong")
        if (world.isClient) {
            return ActionResult.SUCCESS
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
            return ActionResult.CONSUME
        }
    }
    override fun createScreenHandlerFactory(
        state: BlockState?,
        world: World?,
        pos: BlockPos?
    ): NamedScreenHandlerFactory {
        //logger.atInfo().log("is worky?")
        return SimpleNamedScreenHandlerFactory({ syncId: Int, inventory: PlayerInventory, player: PlayerEntity -> run {
            val held = player.handItems.filter { it.isOf(ModItems.wand) }
            if (held.lastIndex == -1) return@run null
            val wand = held[0]
            //val nbt = wand.orCreateNbt
            //logger.atInfo().log("is wand")
            val spells = ModItems.wand.spellList(wand).map { if (it != "") mkMateria(it) else null}.filterNotNull()
            val mods = wand.orCreateNbt.getIntArray("mods").map { mkModMateria(mkMod(it), "nova_arcana:item/mat-blank") }
            val inv: MutableList<ItemStack> = mutableListOf()
            for (i in 0..maxOf(spells.lastIndex, mods.lastIndex)) {
                try {
                    inv += spells[i]
                } catch (_: IndexOutOfBoundsException) {}
                try {
                    inv += mods[i]
                } catch (_: IndexOutOfBoundsException) {}
            }
            val newInv = StaffScrInv(wand.copyAndEmpty(), inv)
            return@run GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, newInv)
            }
        }, TITLE)
    }
}
/*
class StaffScreenHandler(type: ScreenHandlerType<StaffScreenHandler>,  syncId: Int): ScreenHandler {
    val inventory = StaffScrInv()
    var plrInv: PlayerInventory? = null
    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        TODO("Not yet implemented")
    }
    override fun canUse(player: PlayerEntity): Boolean {
        TODO("Not yet implemented")
    }
    companion object {
        fun init(type: ScreenHandlerType<StaffScreenHandler>, syncId: Int, plrInv: PlayerInventory): StaffScreenHandler {
            val h = StaffScreenHandler(type, syncId)
            h.plrInv = plrInv
            return h
        }
    }
}
 */
class StaffScrInv(val wand: ItemStack, inv: List<ItemStack>): ImplementedInventory {
    private val logger = LoggerFactory.getLogger("nova_arcana")
    var storage = run {
        val list = DefaultedList.ofSize<ItemStack>(30, ItemStack.EMPTY)
        for ((num, item) in inv.withIndex()) {
            list[num] = item
        }
        return@run list
    }
    override val items: DefaultedList<ItemStack?>
        get() = storage
    override fun onClose(player: PlayerEntity) {
        super.onClose(player)
        val spells: MutableList<String> = mutableListOf()
        val mods: MutableList<Int> = mutableListOf()
        wand.orCreateNbt.putString("avail_spells", "")
        wand.orCreateNbt.putIntArray("mods", mods)
        val max = wand.orCreateNbt.getInt("slots_f") * 2 + wand.orCreateNbt.getInt("slots_s")
        var count = 0
        for (item in storage) {
            val nbt = item.orCreateNbt
            if (count >= max) {
                player.giveItemStack(item)
                continue
            }
            if (item.item == ModItems.materia) {
                spells += nbt.getString("spell")
                count++
            } else if (item.item == ModItems.modMateria) {
                mods += nbt.getInt("modifier")
                count++
            } else {
                player.giveItemStack(item)
            }
        }
        wand.orCreateNbt.putString("avail_spells", spells.joinToString("\n"))
        wand.orCreateNbt.putIntArray("mods", mods)
        player.giveItemStack(wand)
    }
    override fun isValid(slot: Int, stack: ItemStack): Boolean {
        val nbt = wand.orCreateNbt
        logger.atInfo().log("${stack.item}")
        val h = storage.filter { !it.isEmpty }
        if (h.lastIndex+1 >= (nbt.getInt("slots_f")) * 2 + (nbt.getInt("slots_s"))) return false
        if (stack.item == ModItems.materia || stack.item == ModItems.modMateria) return true
        return false
    }
}