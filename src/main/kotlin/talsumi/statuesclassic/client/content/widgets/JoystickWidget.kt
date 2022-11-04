/*
 * MIT License
 *
 * Copyright (c) 2022 Talsumi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package talsumi.statuesclassic.client.content.widgets

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import talsumi.marderlib.screen.EnhancedScreen
import talsumi.marderlib.screen.widget.BaseWidget
import talsumi.marderlib.util.RenderUtil

class JoystickWidget(x: Int, y: Int, width: Int, height: Int, val stickSize: Int, val u: Int, val v: Int, val screen: EnhancedScreen<*>, val tooltip: Text? = null, val leftSideTooltip: Boolean = false, val callback: (() -> Unit)? = null) : BaseWidget(x, y, width, height) {

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

        //Render the joystick ourselves, vanilla's drawTexture rounds to integers.
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = matrices.peek().positionMatrix

        val texWidth = 256f
        val texHeight = 256f
        val xMin = x
        val xMax = x + stickSize
        val yMin = y
        val yMax = y + stickSize
        val uMin = u / texWidth
        val uMax = (u + stickSize) / texWidth
        val vMin = v / texHeight
        val vMax = (v + stickSize) / texHeight

        bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, xMin, yMax, zOffset.toFloat()).texture(uMin, vMax).next()
        bufferBuilder.vertex(matrix, xMax, yMax, zOffset.toFloat()).texture(uMax, vMax).next()
        bufferBuilder.vertex(matrix, xMax, yMin, zOffset.toFloat()).texture(uMax, vMin).next()
        bufferBuilder.vertex(matrix, xMin, yMin, zOffset.toFloat()).texture(uMin, vMin).next()
        BufferRenderer.drawWithShader(bufferBuilder.end())

        matrices.pop()
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

    override fun onLeftClicked(mouseX: Double, mouseY: Double)
    {
        selected = true
    }

    override fun onRightClicked(mouseX: Double, mouseY: Double)
    {
        //Reset stick to centre
        stickX = workingWidth.toFloat()/2+halfStickSize
        stickY = workingHeight.toFloat()/2+halfStickSize
        callback?.invoke()
    }

    override fun onLeftDragged(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double)
    {
        if (selected) {
            stickX = (stickX + deltaX.toFloat()).coerceIn(halfStickSize, workingWidth+halfStickSize)
            stickY = (stickY + deltaY.toFloat()).coerceIn(halfStickSize, workingHeight+halfStickSize)
            callback?.invoke()
        }
    }

    override fun getTooltip(): List<Text>? = if (tooltip != null) listOf(tooltip) else null
}