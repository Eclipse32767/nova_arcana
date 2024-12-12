package kit.nova_arcana.blocks

import io.github.fabricators_of_create.porting_lib.tags.Tags
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import org.slf4j.LoggerFactory

class VesselRenderer(context: BlockEntityRendererFactory.Context): BlockEntityRenderer<ManaVesselEntity> {
    val logger = LoggerFactory.getLogger("vessel renderer")
    override fun render(
        entity: ManaVesselEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrices.push()
        val client = MinecraftClient.getInstance()
        val mdl = "nova_arcana:item/mana-" + when (entity.manaFilter) {
            ManaFilter.FIRE -> "fire"
            ManaFilter.ICE -> "ice"
            ManaFilter.WIND -> "wind"
            ManaFilter.EARTH -> "earth"
            ManaFilter.SPIRIT -> "spirit"
            ManaFilter.VOID -> "void"
        }
        //val mdl = "nova_arcana:item/mana-fire"
        val loaded = client.bakedModelManager.getModel(Identifier(mdl))
        if (loaded == null) {
            matrices.pop()
            return
        }
        val baseScale = 10.0f/16.0f
        val vScale = 9.0f/16.0f
        val fillAmt = (entity.contents.toFloat() / maxOf(entity.max.toFloat(), 400f))
        if (fillAmt == 0.0f) {
            matrices.pop()
            return
        }
        val totalV = vScale * fillAmt
        //logger.atInfo().log("$fillAmt: ${entity.contents}, ${entity.max}")
        matrices.translate(0.5, (9.0/16.0) - ((1.0-totalV)/2.0), 0.5)
        matrices.scale(baseScale, totalV, baseScale)

        //logger.atInfo().log("model loaded")
        client.itemRenderer.renderItem(Items.GOLD_BLOCK.defaultStack, ModelTransformationMode.NONE, false, matrices, vertexConsumers, light, overlay, loaded)
        matrices.pop()
    }
}