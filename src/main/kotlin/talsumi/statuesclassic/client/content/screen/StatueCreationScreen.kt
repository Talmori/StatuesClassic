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

import com.mojang.authlib.AuthenticationService
import com.mojang.authlib.BaseUserAuthentication
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.BaseMinecraftSessionService
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.yggdrasil.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.widgets.ButtonWidget
import talsumi.statuesclassic.client.content.widgets.JoystickWidget
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.marderlib.screen.EnhancedScreen
import java.util.*

class StatueCreationScreen(handler: StatueCreationScreenHandler, inventory: PlayerInventory?, title: Text?) :
    EnhancedScreen<StatueCreationScreenHandler>(handler, inventory, title, Identifier(StatuesClassic.MODID, "textures/gui/statue_creation.png")) {

    private val joystick1: JoystickWidget
    private val joystick2: JoystickWidget
    private val joystick3: JoystickWidget
    private val joystick4: JoystickWidget
    private val joystick5: JoystickWidget
    private val joystick6: JoystickWidget
    private lateinit var nameField: TextFieldWidget
    private var lastName: String = ""
    private var lookupDelay = 0
    private var data = StatueData("", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

    init
    {
        backgroundWidth = 198
        backgroundHeight = 208
        joystick1 = JoystickWidget(5, 7, 48, 48, 14, 198, 0, this, LiteralText("LeftArm"), ::joystickChange)
        joystick2 = JoystickWidget(145, 7, 48, 48, 14, 198, 0, this, LiteralText("RightArm"), ::joystickChange)
        joystick3 = JoystickWidget(5, 59, 48, 48, 14, 198, 0, this, LiteralText("LeftLeg"), ::joystickChange)
        joystick4 = JoystickWidget(145, 59, 48, 48, 14, 198, 0, this, LiteralText("RightLeg"), ::joystickChange)
        joystick5 = JoystickWidget(5, 111, 48, 48, 14, 198, 0, this, LiteralText("Head"), ::joystickChange)
        joystick6 = JoystickWidget(145, 111, 48, 48, 14, 198, 0, this, LiteralText("Master"), ::joystickChange)
        val randomizeButton = ButtonWidget(4, 162, 190, 20, 0, 208, ::randomize, TranslatableText("gui.statuesclassic.randomize"))
        val formButton = ButtonWidget(4, 184, 190, 20, 0, 208, ::form, TranslatableText("gui.statuesclassic.sculpt"))

        addWidgets(joystick1, joystick2, joystick3, joystick4, joystick5, joystick6, randomizeButton, formButton)
    }

    override fun init()
    {
        super.init()
        nameField = TextFieldWidget(textRenderer, x+59, y+139, 80, 12, TranslatableText("gui.statuesclassic.player_name_field"))
        nameField.setEditable(true)
        nameField.setEditableColor(-1)
        nameField.setUneditableColor(-1)
        nameField.setDrawsBackground(false)
        addSelectableChild(nameField)
        setInitialFocus(nameField)
    }

    private fun joystickChange()
    {
        data.leftArmRaise = joystick1.getYPosition()
        data.leftArmRotate = joystick1.getXPosition()
        data.rightArmRaise = joystick2.getYPosition()
        data.rightArmRotate = joystick2.getXPosition()
        data.leftLegRaise = joystick3.getYPosition()
        data.leftLegRotate = joystick3.getXPosition()
        data.rightLegRaise = joystick4.getYPosition()
        data.rightLegRotate = joystick4.getXPosition()
        data.headRaise = joystick5.getYPosition()
        data.headRotate = joystick5.getXPosition()
        data.masterRaise = joystick6.getYPosition()
        data.masterRotate = joystick6.getXPosition()
    }

    fun form()
    {
        val data = StatueData(
            nameField.text,
        180f * joystick1.getYPosition(),
            180f * joystick1.getXPosition(),
            180f * joystick2.getYPosition(),
            180f * joystick2.getXPosition(),
            180f * joystick3.getYPosition(),
            180f * joystick3.getXPosition(),
            180f * joystick4.getYPosition(),
            180f * joystick4.getXPosition(),
            180f * joystick5.getYPosition(),
            180f * joystick5.getXPosition(),
            180f * joystick6.getYPosition(),
            180f * joystick6.getXPosition())
    }

    fun randomize()
    {
        for (widget in widgets)
            if (widget is JoystickWidget)
                widget.setPosition(Math.random().toFloat() * 2f -1f,Math.random().toFloat() * 2f -1f)
    }

    override fun handledScreenTick()
    {
        if ((nameField.text.isNotEmpty() && nameField.text != lastName) || lookupDelay > 0) {
            if (lookupDelay >= 20) {
                lookupDelay = 0
                //MinecraftClient.getInstance().skinProvider.loadSkin(profile, ::skinAvailable, true)
            }
            lookupDelay++
            lastName = nameField.text
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)
    {
        super.render(matrices, mouseX, mouseY, delta)
        nameField.render(matrices, mouseX, mouseY, delta)
    }

    private fun skinAvailable(type: MinecraftProfileTexture.Type, id: Identifier, texture: MinecraftProfileTexture)
    {
        println("Skin available: $id")
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

    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) = Unit
}