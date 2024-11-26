package kit.nova_arcana

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.registry.Registry
import net.minecraft.util.math.Vec3d
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.sqrt



object TemplateMod : ModInitializer {
	private val logger = LoggerFactory.getLogger("nova_arcana")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state../
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		val itms = ModItems
		val entities = ModEntities
		val recipes = Recipes
		val blks = ModBlocks
		mkMainGrp()
		regSpells()
		var tickcnt = 0;
		ServerTickEvents.START_WORLD_TICK.register { wld ->
			run {
				tickcnt++
				for (plr in wld.getPlayers({true})) {
					val h = ManaHandle(plr)
					if (h.mana > h.manacap) h.mana = h.manacap
					h.castTimer--
					if (h.castTimer < 0) h.castTimer = 0
					if (tickcnt % 5 == 0) {
						val castPenalty = 1.0 - (h.castTimer.toDouble() / 100.0)
						val spd = plr.pos.distanceTo(h.lastpos)
						val mvPenalty = 1.0 - minOf(spd, 1.0)
						var regen = 8.0
						regen *= castPenalty
						regen *= mvPenalty
						h.lastRegen = regen.toInt()
						h.mana += regen.toInt()
					}
					h.lastpos = plr.pos

					//logger.atInfo().log("vels: ${vel.x}, ${vel.y}, ${vel.z}")
					//logger.atInfo().log("regen: $regen, castPenalty: $castPenalty, spd: $spd, mvPenalty: $mvPenalty")
					if (h.manacap < 100) h.manacap = 100
					if (h.mana < 0) h.mana = 0
					if (h.mana > h.manacap) h.mana = h.manacap
					h.syncMana()
				}
			}
		}
	}
}