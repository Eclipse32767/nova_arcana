package kit.nova_arcana.blocks

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.BlockItem
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.world.LightType
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import kotlin.math.sin

class PedestalRenderer(context: BlockEntityRendererFactory.Context): BlockEntityRenderer<PedestalEntity> {
    override fun render(
        entity: PedestalEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val logger = LoggerFactory.getLogger("pedestal renderer")
        //logger.atInfo().log("am render")
        val world = entity.world
        val itemRenderer = MinecraftClient.getInstance().itemRenderer
        val stack = entity.getStack(0)
        matrices.push()
        if (world != null) {
            val offset = sin((world.time + tickDelta) / 8.0) / 4.0;
            matrices.translate(0.5, 1.25 + offset, 0.5)
            if (stack.item is BlockItem) matrices.scale(1.5f, 1.5f, 1.5f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((world.time + tickDelta) * 4));
            //logger.atInfo().log("stack is a ${stack.item}")
            itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, getLightLevel(world, entity.pos), OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 1)
        } else logger.atInfo().log("world is null")
        matrices.pop()
    }
    private fun getLightLevel(world: World, pos: BlockPos): Int {
        val bLight = world.getLightLevel(LightType.BLOCK, pos)
        val sLight = world.getLightLevel(LightType.SKY, pos)
        return LightmapTextureManager.pack(bLight, sLight)
    }
}