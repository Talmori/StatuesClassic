package talsumi.statuesclassic.client.content.widgets

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import talsumi.marderlib.compat.MLCompatRendering
import talsumi.marderlib.screen.EnhancedScreen
import talsumi.marderlib.screen.widget.BaseWidget
import talsumi.marderlib.util.RenderUtil

class JoystickWidget(screen: EnhancedScreen<*>, x: Int, y: Int, width: Int, height: Int, val stickSize: Int, val u: Int, val v: Int, val tooltip: Text? = null, val leftSideTooltip: Boolean = false, val callback: (() -> Unit)? = null) : BaseWidget(x, y, width, height, screen) {

    val workingWidth = width-stickSize
    val workingHeight = height-stickSize
    var halfStickSize = stickSize/2f
    var stickX = halfStickSize+workingWidth.toFloat()/2
    var stickY = halfStickSize+workingHeight.toFloat()/2

    var selected = false

    override fun doRender(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float)
    {
        val x = this.x + x + stickX - halfStickSize
        val y = this.y + y + stickY - halfStickSize

        val snap = RenderUtil.getSnapshot()
        matrices.push()

        unboundDrawTexture(matrices, x, y, stickSize.toFloat(), stickSize.toFloat(), u, v)

        snap.restore()
    }

    fun getPosition(): Pair<Float, Float>
    {
        val x = ((stickX-halfStickSize)/(workingWidth)*2)-1
        val y = ((stickY-halfStickSize)/(workingHeight)*2)-1
        return Pair(x, y)
    }

    fun getXPosition(): Float = ((stickX-halfStickSize)/(workingWidth)*2)-1
    fun getYPosition(): Float = ((stickY-halfStickSize)/(workingHeight)*2)-1

    fun setPosition(x: Float, y: Float)
    {
        stickX = ((x+1)/2) * workingWidth + halfStickSize
        stickY = ((y+1)/2) * workingHeight + halfStickSize
        callback?.invoke()
    }

    private fun getBoundsOfStick(): Pair<Vec2f, Vec2f>
    {
        val xOff = screen.getScreenX() + x
        val yOff = screen.getScreenY() + y

        return Pair(Vec2f(xOff+stickX, yOff+stickY), Vec2f(xOff+stickX+stickSize, yOff+stickY+stickSize))
    }

    override fun renderTooltip(screen: EnhancedScreen<*>, matrices: MatrixStack, mouseX: Int, mouseY: Int)
    {
        val text = getTooltip()?.get(0) ?: return
        val mc = MinecraftClient.getInstance()

        val x = if (leftSideTooltip)
            screen.getScreenX() + x - mc.textRenderer.getWidth(text) - (width / 2)
        else
            screen.getScreenX() + x + width

        screen.renderTooltip(matrices, text, x, screen.getScreenY() + y + (height / 2) + (mc.textRenderer.fontHeight))
    }

    private fun isMouseOverStick(mouseX: Double, mouseY: Double): Boolean
    {
        val bounds = getBoundsOfStick()
        return mouseX >= bounds.first.x && mouseX <= bounds.second.x && mouseY >= bounds.first.y && mouseY <= bounds.second.y
    }

    override fun onMouseAction(mouseX: Double, mouseY: Double, button: Button, type: Type)
    {
        if (type == Type.PRESSED) {
            if (button == Button.LEFT) {
                selected = true
            }
            else {
                //Reset stick to centre
                stickX = workingWidth.toFloat()/2+halfStickSize
                stickY = workingHeight.toFloat()/2+halfStickSize
                callback?.invoke()
            }

        }
    }

    override fun onDragged(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double, type: Button)
    {
        if (type == Button.LEFT && selected) {
            stickX = (stickX + deltaX.toFloat()).coerceIn(halfStickSize, workingWidth+halfStickSize)
            stickY = (stickY + deltaY.toFloat()).coerceIn(halfStickSize, workingHeight+halfStickSize)
            callback?.invoke()
        }
    }

    override fun getTooltip(): List<Text>? = if (tooltip != null) listOf(tooltip) else null

    fun unboundDrawTexture(matrices: MatrixStack, x: Float, y: Float, width: Float, height: Float, u: Int, v: Int, zOffset: Float = 0f, texWidth: Float = 256f, texHeight: Float = 256f)
    {
        val snap = RenderUtil.getSnapshot()
        matrices.push()

        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = matrices.peek().positionMatrix

        val xMin = x
        val xMax = x + width
        val yMin = y
        val yMax = y + height
        val uMin = u / texWidth
        val uMax = (u + width) / texWidth
        val vMin = v / texHeight
        val vMax = (v + height) / texHeight

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, xMin, yMax, zOffset).texture(uMin, vMax).next()
        bufferBuilder.vertex(matrix, xMax, yMax, zOffset).texture(uMax, vMax).next()
        bufferBuilder.vertex(matrix, xMax, yMin, zOffset).texture(uMax, vMin).next()
        bufferBuilder.vertex(matrix, xMin, yMin, zOffset).texture(uMin, vMin).next()
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())

        matrices.pop()
        snap.restore()
    }


    override fun isFocused(): Boolean = false
    override fun setFocused(focused: Boolean) = Unit
}