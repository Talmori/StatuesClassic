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

import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.render.blockentity.StatueBERenderer
import talsumi.statuesclassic.client.content.widgets.ButtonWidget
import talsumi.statuesclassic.client.content.widgets.JoystickWidget
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.core.StatueCreation
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.marderlib.screen.EnhancedScreen
import talsumi.statuesclassic.marderlib.util.RenderUtil
import talsumi.statuesclassic.networking.ClientPacketsOut
import java.util.*

class StatueCreationScreen(handler: StatueCreationScreenHandler, inventory: PlayerInventory?, title: Text?) :
    EnhancedScreen<StatueCreationScreenHandler>(handler, inventory, title, Identifier(StatuesClassic.MODID, "textures/gui/statue_creation_overlay.png")) {

    private val underlayTexture = Identifier(StatuesClassic.MODID, "textures/gui/statue_creation_underlay.png")
    private val joystick1: JoystickWidget
    private val joystick2: JoystickWidget
    private val joystick3: JoystickWidget
    private val joystick4: JoystickWidget
    private val joystick5: JoystickWidget
    private val joystick6: JoystickWidget
    private lateinit var nameField: TextFieldWidget
    private var lastName: String = ""
    private var uuid: UUID? = null
    private var lookupDelay = 0
    private var data = StatueData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var skin: Identifier? = null

    private val renderer = StatueBERenderer()

    private val fullbright = 0xF000F0

    init
    {
        backgroundWidth = 198
        backgroundHeight = 208
        joystick1 = JoystickWidget(145, 7, 48, 48, 14, 198, 0, this, LiteralText("LeftArm"), ::joystickChange)
        joystick2 = JoystickWidget(5, 7, 48, 48, 14, 198, 0, this, LiteralText("RightArm"), ::joystickChange)
        joystick3 = JoystickWidget(145, 59, 48, 48, 14, 198, 0, this, LiteralText("LeftLeg"), ::joystickChange)
        joystick4 = JoystickWidget(5, 59, 48, 48, 14, 198, 0, this, LiteralText("RightLeg"), ::joystickChange)
        joystick5 = JoystickWidget(5, 111, 48, 48, 14, 198, 0, this, LiteralText("Head"), ::joystickChange)
        joystick6 = JoystickWidget(145, 111, 48, 48, 14, 198, 0, this, LiteralText("Master"), ::joystickChange)
        val randomizeButton = ButtonWidget(4, 162, 190, 20, 0, 208, ::randomize, {TranslatableText("gui.statuesclassic.randomize")})
        val formButton = object: ButtonWidget(4, 184, 190, 20, 0, 208, ::form, {TranslatableText("gui.statuesclassic.sculpt")}, { uuid != null }) {
            override fun getTooltip(): List<Text>?
            {
                return if (uuid == null) listOf(TranslatableText("gui.statuesclassic.sculpt_invalid")) else null
            }
        }

        addWidgets(joystick1, joystick2, joystick3, joystick4, joystick5, joystick6, randomizeButton, formButton)
    }

    fun receiveUuid(username: String, uuid: UUID)
    {
        if (username == lastName)
            this.uuid = uuid
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
        StatueCreation.applyJoystickAnglesToStatueData(data,
            joystick1.getYPosition(),
            joystick1.getXPosition(),
            joystick2.getYPosition(),
            joystick2.getXPosition(),
            joystick3.getYPosition(),
            joystick3.getXPosition(),
            joystick4.getYPosition(),
            joystick4.getXPosition(),
            joystick5.getYPosition(),
            joystick5.getXPosition(),
            joystick6.getYPosition(),
            joystick6.getXPosition())
    }

    fun form()
    {
        if (uuid == null)
            return

        ClientPacketsOut.sendFormStatuePacket(uuid!!, data)
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
            lookupDelay++

            //Make sure not to spam lookup requests.
            if (lookupDelay >= 40) {
                lookupDelay = 0
                //This will eventually update the uuid in this screen.
                ClientPacketsOut.sendLookupUuidPacket(lastName)
            }

            //Invalidate uuid and skin when input changes
            if (lastName != nameField.text) {
                uuid = null
                skin = null
            }

            lastName = nameField.text
            if (lastName.isEmpty()) {
                uuid = null
                skin = null
            }
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float)
    {
        super.render(matrices, mouseX, mouseY, delta)
        nameField.render(matrices, mouseX, mouseY, delta)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int)
    {
        //Render underlay
        RenderSystem.setShaderTexture(0, underlayTexture)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)

        //Render player model
        val middleX = x+backgroundWidth/2.0
        val middleY = y+backgroundHeight/2.0
        drawModel(matrices, 45f, middleX, middleY-65f, mouseX, mouseY, delta)

        //Render ui
        super.drawBackground(matrices, delta, mouseX, mouseY)
    }

    private fun drawModel(matrices: MatrixStack, size: Float, x: Double, y: Double, mouseX: Int, mouseY: Int, delta: Float)
    {
        //I have no idea what some of this is, it's just copied from InventoryScreen#drawEntity
        if (uuid != null) {
            val skinData = StatueBERenderer.getCachedData(uuid!!) ?: return

            matrices.push()
            val snapshot = RenderUtil.getSnapshot()

            val viewStack = RenderSystem.getModelViewStack()
            viewStack.push()
            viewStack.translate(x, y, 1050.0)
            viewStack.scale(1.0f, 1.0f, -1.0f)
            RenderSystem.applyModelViewMatrix()

            val ourMatrix = MatrixStack()
            ourMatrix.translate(0.0, 0.0, 1000.0)
            ourMatrix.scale(size, -size, -size)

            DiffuseLighting.method_34742()

            val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
            RenderSystem.runAsFancy {
                renderer.render(null, data, uuid!!, skinData.slim, skinData.texture!!, delta, ourMatrix, immediate, fullbright, OverlayTexture.DEFAULT_UV)
            }
            immediate.draw()

            viewStack.pop()
            RenderSystem.applyModelViewMatrix()
            DiffuseLighting.enableGuiDepthLighting()

            snapshot.restore()
            matrices.pop()
        }
    }

    private fun skinAvailable(type: MinecraftProfileTexture.Type, id: Identifier, texture: MinecraftProfileTexture)
    {
        skin = id
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
    {
        if (keyCode == 256)
            client!!.player!!.closeHandledScreen()
        return if (!nameField.keyPressed(keyCode, scanCode, modifiers) && !nameField.isActive) super.keyPressed(keyCode, scanCode, modifiers) else true
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