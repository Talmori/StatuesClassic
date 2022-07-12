package talsumi.statuesclassic.marderlib.screen

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import talsumi.statuesclassic.marderlib.screen.widget.BaseWidget
import kotlin.reflect.KClass

abstract class EnhancedScreen<T: ScreenHandler>(handler: T, inventory: PlayerInventory?, title: Text?, val backgroundTex: Identifier) : HandledScreen<T>(handler, inventory, title) {

    val widgets = ArrayList<BaseWidget>()

    fun addWidgets(vararg widgets: BaseWidget)
    {
        for (widget in widgets)
            this.widgets.add(widget)
    }

    /**
     * Returns the first widget of [type]
     */
    fun <T: BaseWidget> getWidget(type: KClass<T>): T?
    {
        for (widget in widgets)
            if (type.isInstance(widget))
                return widget as T

        return null
    }

    /**
     * Returns all widgets of [type]
     */
    fun <T: BaseWidget> getWidgets(type: KClass<T>): List<T>
    {
        val list = ArrayList<T>()

        for (widget in widgets)
            if (type.isInstance(widget))
                list.add(widget as T)

        return list
    }

    /**
     * Returns all widgets
     */
    fun getWidgets(): List<BaseWidget> = widgets

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int)
    {
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, backgroundTex)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        drawScreenElements(matrices, x, y, delta, mouseX, mouseY)
        for (widget in widgets)
            widget.doRender(matrices, x, y, mouseX, mouseY, delta)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)
    {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        for (widget in widgets)
            if (widget.isHovered(widget.x + x, widget.y + y, mouseX, mouseY))
                if (widget.getTooltip() != null)
                    renderTooltip(matrices, widget.getTooltip(), mouseX, mouseY)
    }

    open fun drawScreenElements(matrices: MatrixStack, x: Int, y: Int, delta: Float, mouseX: Int, mouseY: Int) = Unit

    override fun init()
    {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean
    {
        for (widget in widgets) {
            if (widget.isHovered(widget.x + x, widget.y + y, mouseX.toInt(), mouseY.toInt())) {
                widget.onGeneralClicked(mouseX, mouseY)
                if (button == 0)
                    widget.onLeftClicked(mouseX, mouseY)
                else
                    widget.onRightClicked(mouseX, mouseY)

                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}