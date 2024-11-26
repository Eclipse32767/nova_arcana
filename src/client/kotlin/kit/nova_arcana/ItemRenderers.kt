package kit.nova_arcana

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import org.slf4j.Logger

class MultiPartRender(val logger: Logger, val fullbright: Boolean, val fn: (ItemStack, ModelTransformationMode) -> List<String>): DynamicItemRenderer {
    override fun render(
        stk: ItemStack,
        mode: ModelTransformationMode,
        mat: MatrixStack,
        vtxConsumer: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        mat.push()
        val client = MinecraftClient.getInstance()
        mat.translate(0.5, 0.5, 0.5)
        for (path in fn(stk, mode)) {
            val mdl = client.bakedModelManager.getModel(Identifier(path))
            if (mdl != null) {
                //logger.atInfo().log(light.toString())
                client.itemRenderer.renderItem(stk, mode, false, mat, vtxConsumer, light, if (fullbright) 0 else overlay, mdl)
            } else {
                logger.atInfo().log("Model Not Found: $path")
            }
        }
        mat.pop()
    }
}

fun regItemRenderers(logger: Logger) {
    val wandRender = MultiPartRender(logger, false) { stk, mode -> run {
        val nbt = stk.orCreateNbt
        val base = nbt.getString("core")
        val decor = nbt.getString("decor")
        val gem = nbt.getString("gem")
        listOf(base, decor, gem)
    }}
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.wand, wandRender)
    val partRender = MultiPartRender(logger, false) {stk, mode -> listOf(stk.orCreateNbt.getString("model"))}
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.wandCore, partRender)
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.wandDecor, partRender)
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.wandGem, partRender)
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.modMateria, MultiPartRender(logger, true) {stk, mode -> listOf(
        mkMod(stk.orCreateNbt.getInt("modifier")).model()
    )})
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.materia, MultiPartRender(logger, true) {stk, mode -> listOf(
        spellReg[Identifier(stk.orCreateNbt.getString("spell"))]?.sprite.toString())})
}