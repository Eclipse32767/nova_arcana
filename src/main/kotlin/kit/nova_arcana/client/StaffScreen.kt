package kit.nova_arcana.client

import net.minecraft.util.Identifier

private val TEXTURE = Identifier("nova_arcana:textures/gui/staff_workbench")
/*
class StaffScreen(handler: StaffScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<StaffScreenHandler>(handler, inventory, title) {
    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawBackground(context, delta, mouseX, mouseY)
        super.render(context, mouseX, mouseY, delta)
    }
}

 */