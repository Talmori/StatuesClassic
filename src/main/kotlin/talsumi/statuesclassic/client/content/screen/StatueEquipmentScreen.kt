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

package talsumi.statuesclassic.client.content.screen

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import talsumi.marderlib.screen.EnhancedScreen
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.widgets.JoystickWidget
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler
import talsumi.statuesclassic.networking.ClientPacketsOut

class StatueEquipmentScreen(handler: StatueEquipmentScreenHandler, inventory: PlayerInventory?, title: Text?) :
    EnhancedScreen<StatueEquipmentScreenHandler>(handler, inventory, title, Identifier(StatuesClassic.MODID, "textures/gui/statue_equipment.png")) {

    private val leftJoystick: JoystickWidget
    private val rightJoystick: JoystickWidget

    var delay = 0f
    var setup = false

    init
    {
        backgroundHeight = 179
        leftJoystick = JoystickWidget(7, 67, 51, 14, 14, 176, 0, this,
            TranslatableText("gui.statuesclassic.joystick.equipment_left_hand"), true, callback = ::joystickChange)
        rightJoystick = JoystickWidget(117, 67, 51, 14, 14, 176, 0, this,
            TranslatableText("gui.statuesclassic.joystick.equipment_right_hand"), callback = ::joystickChange)

        addWidgets(leftJoystick, rightJoystick)

        leftJoystick.setPosition(screenHandler.statue?.rightHandRotate ?: 0f, 0f)
        rightJoystick.setPosition(screenHandler.statue?.leftHandRotate ?: 0f, 0f)

        setup = true
    }

    fun joysticksUpdatedFromServer(left: Float, right: Float)
    {
        leftJoystick.stickX = left.coerceIn(-1f, 1f)
        rightJoystick.stickX = right.coerceIn(-1f, 1f)
    }

    private fun joystickChange()
    {
        if (!setup) return //Ignore changes until [init] has finished.
        val statue = handler?.statue ?: return

        //Update rotation on client only. When joysticks are released, an update packet will be sent to the server. (See mouseReleased)
        statue.rightHandRotate = leftJoystick.getXPosition()
        statue.leftHandRotate = rightJoystick.getXPosition()
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean
    {
        var dragged = false
        for (widget in widgets) {
            widget.onGeneralDragged(mouseX, mouseY, deltaX, deltaY)
            if (button == 0)
                widget.onLeftDragged(mouseX, mouseY, deltaX, deltaY)
            else
                widget.onRightDragged(mouseX, mouseY, deltaX, deltaY)

            dragged = true
        }
        return if (dragged) true else return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean
    {
        for (widget in widgets) {
            if (widget is JoystickWidget)
                widget.selected = false
        }

        //Send rotation update packet when joysticks are released
        ClientPacketsOut.sendUpdateStatueHandsPacket(
            leftJoystick.getXPosition(),
            rightJoystick.getXPosition())

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) = Unit
}