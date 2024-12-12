package kit.nova_arcana

import kit.nova_arcana.blocks.PedestalRenderer
import kit.nova_arcana.blocks.VesselRenderer
import kit.nova_arcana.client.regItemRenderers
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.util.InputUtil
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory





object TemplateModClient : ClientModInitializer {
	private val logger = LoggerFactory.getLogger("nova_arcana")
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(ModEntities.FireballProjType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.DrainBeamType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.SiphonHealType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.ExcavateItemType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.PlacementType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.ImmolateType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.InfusionParticleType) {FlyingItemEntityRenderer(it)}
		EntityRendererRegistry.register(ModEntities.ManaBeamType) {FlyingItemEntityRenderer(it)}

		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INFUSION_STONE, RenderLayer.getTranslucent())
		for (blk in listOf(ModBlocks.MANA_VESSEL_ICE, ModBlocks.MANA_VESSEL_FIRE, ModBlocks.MANA_VESSEL_VOID, ModBlocks.MANA_VESSEL_WIND, ModBlocks.MANA_VESSEL_EARTH, ModBlocks.MANA_VESSEL_SPIRIT)) {
			BlockRenderLayerMap.INSTANCE.putBlock(blk, RenderLayer.getCutout())
		}

		ModelLoadingPlugin.register {ctx -> run {
			val idStrs = listOf("item/wand-core-basic", "item/wand-claw-basic", "item/wand-orb-basic", "item/wand-gem-emerald", "item/mat-blank",
				"item/mat-pwr", "item/mat-eff", "item/mat-area", "item/mat-flame", "item/mat-siphon", "item/mat-excavate",
				"item/mat-support", "item/mat-dash", "item/mat-recovery", "item/mana-fire", "item/mana-ice", "item/mana-earth", "item/mana-wind", "item/mana-void", "item/mana-spirit")
			for (id in idStrs) {
				ctx.addModels(Identifier("nova_arcana:$id"))
			}
		}}

		logger.atInfo().log("Registering wand renderer...")
		regItemRenderers(logger)
		logger.atInfo().log("Registered wand renderer!")
		BlockEntityRendererFactories.register(ModBlockEntities.PEDESTAL_TYPE) { PedestalRenderer(it) }
		BlockEntityRendererFactories.register(ModBlockEntities.MANA_VESSEL_TYPE) {VesselRenderer(it)}
		val swapSpell = KeyBindingHelper.registerKeyBinding(KeyBinding("key.nova_arcana.swap_spell", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.nova_arcana.spellcasting"))
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			run {
				try {
					if (swapSpell.wasPressed()) {
						val world = client.world
						world?.playSound(
							client.player,
							client.player?.blockPos,
							SoundEvent.of(Identifier("block.piston.extend"), 10.0F),
							SoundCategory.PLAYERS
						)
						ClientPlayNetworking.send(ModMessages.SPELLSWAP_ID, PacketByteBufs.create())
					}
				} catch (_: Exception) {}

			}
		}
		var lastMana = 100
		var renderpos = 100
		HudRenderCallback.EVENT.register { ctx, delta ->
			run {
				val plr = MinecraftClient.getInstance().player?:return@run
				val wands = plr.handItems.filter { it.isOf(ModItems.wand) }
				if (wands.lastIndex < 0) return@run
				val wand = wands[0]
				val h = ManaHandle(plr)
				val mana = h.mana
				val manacap = h.manacap
				val mana_scaled = mana * 100 / manacap
				val renderpos_scaled = renderpos * 100 / manacap
				//logger.atInfo().log("$mana, $lastpts")
				/*
				ctx.fill(0, 0, manacap, 5, ColorHelper.Argb.getArgb(255, 130, 130, 130))
				ctx.fill(0, 0, maxOf(renderpos, mana), 5, ColorHelper.Argb.getArgb(255, 232, 232, 232))
				ctx.fill(0, 0, minOf(renderpos, mana), 5, ColorHelper.Argb.getArgb(255, 63, 230, 252))
				 */
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-empty.png"), 0, 0, 100, 10, 0.0f, 0.0f, 100, 10, 100, 10)
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-filling.png"), 0, 0, maxOf(renderpos_scaled, mana_scaled), 10, 0.0f, 0.0f, maxOf(renderpos_scaled, mana_scaled), 10, 100, 10)
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-full.png"), 0, 0, minOf(renderpos_scaled, mana_scaled), 10, 0.0f, 0.0f, minOf(renderpos_scaled, mana_scaled), 10, 100, 10)
				try {
					val spellList = ModItems.wand.spellList(wand)
					val nbt = wand.orCreateNbt
					val spellnum = nbt.getInt("spell")
					val selSpellname = spellReg[Identifier(spellList[spellnum])]?.name
					val modName = try {
						"(${mkMod(nbt.getIntArray("mods")[spellnum]).pretty_name()})"
					} catch (_: IndexOutOfBoundsException) {
						""
					}
					ctx.drawText(
						MinecraftClient.getInstance().textRenderer,
						"${selSpellname?.string ?: ""} $modName", 11, 21, 0xFFFFFF, true
					)
					val spellpkg = spellReg[Identifier(spellList[spellnum])]
					if (spellpkg != null) {
						ctx.drawTexture(
							Identifier(
								spellpkg.sprite.namespace,
								"textures/" + spellpkg.sprite.path + ".png"
							), 0, 20, 10, 10, 0f, 0f, 16, 16, 16, 16
						)
					}
					val nextSpell = if (spellnum >= spellList.lastIndex) 0 else spellnum + 1
					val nextSpellname = spellReg[Identifier(spellList[nextSpell])]?.name
					val nextModName = try {
						"(${mkMod(nbt.getIntArray("mods")[nextSpell]).pretty_name()})"
					} catch (_: IndexOutOfBoundsException) {
						""
					}
					ctx.drawText(
						MinecraftClient.getInstance().textRenderer,
						"${nextSpellname?.string ?: ""} $nextModName", 11, 31, 0xAAAAAA, true
					)
					val nextSpellpkg = spellReg[Identifier(spellList[nextSpell])]
					if (nextSpellpkg != null) {
						ctx.drawTexture(
							Identifier(
								nextSpellpkg.sprite.namespace,
								"textures/" + nextSpellpkg.sprite.path + ".png"
							), 0, 30, 10, 10, 0f, 0f, 16, 16, 16, 16
						)
					}
					val prevSpell = if (spellnum <= 0) spellList.lastIndex else spellnum - 1
					val prevSpellname = spellReg[Identifier(spellList[prevSpell])]?.name
					val prevModName = try {
						"(${mkMod(nbt.getIntArray("mods")[prevSpell]).pretty_name()})"
					} catch (_: IndexOutOfBoundsException) {
						""
					}
					ctx.drawText(
						MinecraftClient.getInstance().textRenderer,
						"${prevSpellname?.string ?: ""} $prevModName", 11, 11, 0xAAAAAA, true
					)
					val prevSpellpkg = spellReg[Identifier(spellList[prevSpell])]
					if (prevSpellpkg != null) {
						ctx.drawTexture(
							Identifier(
								prevSpellpkg.sprite.namespace,
								"textures/" + prevSpellpkg.sprite.path + ".png"
							), 0, 10, 10, 10, 0f, 0f, 16, 16, 16, 16
						)
					}
				} catch (_: Exception) {}
				 //else logger.atInfo().log(spellList[spellnum])
				//if (lastMana != mana) {
				//	renderpos = lastMana
				//	lastMana = mana
				//}
				if (renderpos > mana) {
					renderpos -= 1
				} else if (renderpos < mana) {
					renderpos += 1
				}

				///ctx.drawText(MinecraftClient.getInstance().textRenderer, "mana: $mana / $manacap (+$lastRegen)", 0, 0, 0, false)
			}
		}
	}
}