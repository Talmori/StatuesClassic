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

package talsumi.statuesclassic.client.content.screen

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.widgets.JoystickWidget
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler
import talsumi.statuesclassic.core.StatueCreation
import talsumi.statuesclassic.marderlib.screen.EnhancedScreen
import talsumi.statuesclassic.networking.ClientPacketsOut

//TODO: Equipment screen with preview
class StatueEquipmentScreen(handler: StatueEquipmentScreenHandler, inventory: PlayerInventory?, title: Text?) :
    EnhancedScreen<StatueEquipmentScreenHandler>(handler, inventory, title, Identifier(StatuesClassic.MODID, "textures/gui/statue_equipment.png")) {

    val leftJoystick: JoystickWidget
    val rightJoystick: JoystickWidget

    init
    {
        backgroundHeight = 179
        leftJoystick = JoystickWidget(7, 67, 51, 14, 14, 176, 0, this, LiteralText("LeftHeld"), ::joystickChange)
        rightJoystick = JoystickWidget(117, 67, 51, 14, 14, 176, 0, this, LiteralText("RightHeld"), ::joystickChange)

        addWidgets(leftJoystick, rightJoystick)

        leftJoystick.setPosition(StatueCreation.decodeHandRotation(screenHandler.statue?.leftHandRotate ?: 0f), 0f)
        rightJoystick.setPosition(StatueCreation.decodeHandRotation(screenHandler.statue?.rightHandRotate ?: 0f), 0f)
    }

    private fun joystickChange()
    {
        ClientPacketsOut.sendUpdateStatueHandsPacket(
            StatueCreation.encodeHandRotation(leftJoystick.getXPosition()),
            StatueCreation.encodeHandRotation(rightJoystick.getXPosition()))
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

        return super.mouseReleased(mouseX, mouseY, button)
    }
}