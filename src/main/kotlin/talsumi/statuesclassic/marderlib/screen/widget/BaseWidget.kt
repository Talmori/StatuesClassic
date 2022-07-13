/*
 * MIT License
 *
 *  Copyright (c) 2022 Talsumi
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *
 */

package talsumi.statuesclassic.marderlib.screen.widget

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

abstract class BaseWidget(x: Int, y: Int, width: Int, height: Int): ClickableWidget(x, y, width, height, Text.of("")) {

    override fun appendNarrations(builder: NarrationMessageBuilder?) = Unit

    open fun updateFromPacket(buf: PacketByteBuf)
    {

    }

    open fun doRender(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float)
    {

    }

    open fun onGeneralClicked(mouseX: Double, mouseY: Double)
    {

    }

    open fun onLeftClicked(mouseX: Double, mouseY: Double)
    {

    }

    open fun onRightClicked(mouseX: Double, mouseY: Double)
    {

    }

    open fun onGeneralDragged(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double)
    {

    }

    open fun onLeftDragged(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double)
    {

    }

    open fun onRightDragged(mouseX: Double, mouseY: Double, deltaX: Double, deltaY: Double)
    {

    }

    open fun isHovered(x: Int, y: Int, mouseX: Int, mouseY: Int): Boolean
    {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
    }

    open fun getTooltip(): List<Text>? = null
}