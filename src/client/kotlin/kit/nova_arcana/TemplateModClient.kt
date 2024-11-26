package kit.nova_arcana

import kit.nova_arcana.TemplateModClient.logger
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.render.item.BuiltinModelItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory





object TemplateModClient : ClientModInitializer {
	private val logger = LoggerFactory.getLogger("nova_arcana")
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(ModEntities.FireballProjType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.DrainBeamType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.SiphonHealType) { ctx -> FlyingItemEntityRenderer(ctx) }
		EntityRendererRegistry.register(ModEntities.ExcavateItemType) { ctx -> FlyingItemEntityRenderer(ctx) }

		ModelLoadingPlugin.register {ctx -> run {
			val idStrs = listOf("item/wand-core-basic", "item/wand-claw-basic", "item/wand-orb-basic", "item/wand-gem-emerald", "item/mat-blank",
				"item/mat-pwr", "item/mat-eff", "item/mat-area", "item/mat-flame", "item/mat-siphon", "item/mat-excavate",
				"item/mat-support", "item/mat-dash", "item/mat-recovery")
			for (id in idStrs) {
				ctx.addModels(Identifier("nova_arcana:$id"))
			}
		}}

		logger.atInfo().log("Registering wand renderer...")
		regItemRenderers(logger)
		logger.atInfo().log("Registered wand renderer!")
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
				//logger.atInfo().log("$mana, $lastpts")
				/*
				ctx.fill(0, 0, manacap, 5, ColorHelper.Argb.getArgb(255, 130, 130, 130))
				ctx.fill(0, 0, maxOf(renderpos, mana), 5, ColorHelper.Argb.getArgb(255, 232, 232, 232))
				ctx.fill(0, 0, minOf(renderpos, mana), 5, ColorHelper.Argb.getArgb(255, 63, 230, 252))
				 */
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-empty.png"), 0, 0, 100, 10, 0.0f, 0.0f, 100, 10, 100, 10)
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-filling.png"), 0, 0, maxOf(renderpos, mana), 10, 0.0f, 0.0f, maxOf(renderpos, mana), 10, 100, 10)
				ctx.drawTexture(Identifier("nova_arcana:textures/gui/manabar-full.png"), 0, 0, minOf(renderpos, mana), 10, 0.0f, 0.0f, minOf(renderpos, mana), 10, 100, 10)
				val spellList = ModItems.wand.spellList(wand)
				val nbt = wand.orCreateNbt
				val spellnum = nbt.getInt("spell")
				val selSpellname = spellReg[Identifier(spellList[spellnum])]?.name
				val modName = try {"(${mkMod(nbt.getIntArray("mods")[spellnum]).pretty_name()})"} catch (_: IndexOutOfBoundsException) {""}
				ctx.drawText(MinecraftClient.getInstance().textRenderer,
					"${selSpellname?.string?:""} $modName", 11, 21, 0xFFFFFF, true)
				val spellpkg = spellReg[Identifier(spellList[spellnum])]
				if (spellpkg != null) {
					ctx.drawTexture(Identifier(spellpkg.sprite.namespace, "textures/"+spellpkg.sprite.path+".png"), 0, 20, 10, 10, 0f, 0f, 16, 16, 16, 16 )
				}
				val nextSpell = if (spellnum >= spellList.lastIndex) 0 else spellnum + 1
				val nextSpellname = spellReg[Identifier(spellList[nextSpell])]?.name
				val nextModName = try {"(${mkMod(nbt.getIntArray("mods")[nextSpell]).pretty_name()})"} catch (_: IndexOutOfBoundsException) {""}
				ctx.drawText(MinecraftClient.getInstance().textRenderer,
					"${nextSpellname?.string?:""} $nextModName", 11, 31, 0xAAAAAA, true)
				val nextSpellpkg = spellReg[Identifier(spellList[nextSpell])]
				if (nextSpellpkg != null) {
					ctx.drawTexture(Identifier(nextSpellpkg.sprite.namespace, "textures/"+nextSpellpkg.sprite.path+".png"), 0, 30, 10, 10, 0f, 0f, 16, 16, 16, 16 )
				}
				val prevSpell = if (spellnum <= 0) spellList.lastIndex else spellnum - 1
				val prevSpellname = spellReg[Identifier(spellList[prevSpell])]?.name
				val prevModName = try {"(${mkMod(nbt.getIntArray("mods")[prevSpell]).pretty_name()})"} catch (_: IndexOutOfBoundsException) {""}
				ctx.drawText(MinecraftClient.getInstance().textRenderer,
					"${prevSpellname?.string?:""} $prevModName", 11, 11, 0xAAAAAA, true)
				val prevSpellpkg = spellReg[Identifier(spellList[prevSpell])]
				if (prevSpellpkg != null) {
					ctx.drawTexture(Identifier(prevSpellpkg.sprite.namespace, "textures/"+prevSpellpkg.sprite.path+".png"), 0, 10, 10, 10, 0f, 0f, 16, 16, 16, 16 )
				}
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