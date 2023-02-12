package talsumi.statuesclassic.client.content.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import talsumi.marderlib.screen.EnhancedScreen
import talsumi.marderlib.screen.widget.BaseWidget
import talsumi.marderlib.util.RenderUtil

open class ButtonWidget(screenIn: EnhancedScreen<*>, x: Int, y: Int, width: Int, height: Int, val u: Int, val v: Int, val function: (() -> Unit)? = null, val text: (() -> Text)? = null, val isActive: (() -> Boolean)? = null) : BaseWidget(x, y, width, height, screenIn) {

    override fun doRender(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float)
    {
        val x = this.x + x
        val y = this.y + y

        val snap = RenderUtil.getSnapshot()
        matrices.push()

        var v = v

        val active = isActive?.invoke() != false

        if (!active)
            v += height

        if (isHovered(x, y, mouseX, mouseY) || !active)
            drawTexture(matrices, x, y, u, v, width, height)

        text?.invoke().let { DrawableHelper.drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, it, x + width / 2, y + (height - 8) / 2,  16777215) }

        matrices.pop()
        snap.restore()
    }

    override fun onMouseAction(mouseX: Double, mouseY: Double, button: Button, type: Type)
    {
        if (button == Button.LEFT && type == Type.PRESSED)
            function?.invoke()
    }
}