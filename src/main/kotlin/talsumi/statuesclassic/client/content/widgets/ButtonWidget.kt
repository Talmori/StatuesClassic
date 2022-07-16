package talsumi.statuesclassic.client.content.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import talsumi.statuesclassic.marderlib.screen.widget.BaseWidget
import talsumi.statuesclassic.marderlib.util.RenderUtil

class ButtonWidget(x: Int, y: Int, width: Int, height: Int, val u: Int, val v: Int, val function: (() -> Unit)? = null, val text: Text? = null) : BaseWidget(x, y, width, height) {

    override fun doRender(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float)
    {
        val x = this.x + x
        val y = this.y + y

        val snap = RenderUtil.getSnapshot()
        matrices.push()

        if (isHovered(x, y, mouseX, mouseY))
            drawTexture(matrices, x, y, u, v, width, height)

        if (text != null)
            DrawableHelper.drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, text, x + width / 2, y + (height - 8) / 2,  16777215)

        matrices.pop()
        snap.restore()
    }

    override fun onLeftClicked(mouseX: Double, mouseY: Double)
    {
        function?.invoke()
    }
}