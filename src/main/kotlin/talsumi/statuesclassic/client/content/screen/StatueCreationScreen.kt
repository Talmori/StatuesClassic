package talsumi.statuesclassic.client.content.screen

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import talsumi.marderlib.compat.MLCompatText
import talsumi.marderlib.screen.EnhancedScreen
import talsumi.marderlib.screen.widget.BaseWidget
import talsumi.marderlib.util.RenderUtil
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.render.blockentity.StatueBERenderer
import talsumi.statuesclassic.client.content.widgets.ButtonWidget
import talsumi.statuesclassic.client.content.widgets.JoystickWidget
import talsumi.statuesclassic.client.core.SkinHandler
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.core.StatueHelper
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
    /**
     * The name that was stored in [nameField] the previous tick.
     */
    private var lastName: String = ""
    /**
     * The UUID sent by the server that matches the name queried.
     */
    private var uuid: UUID? = null
    /**
     * The actual name (case-sensitive) of the shown player.
     */
    private var trueName: String? = null
    private var lookupDelay = 0
    private var data = StatueData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var block: BlockState

    private val renderer = StatueBERenderer()

    private val fullbright = 0xF000F0

    init
    {
        backgroundWidth = 198
        backgroundHeight = 208
        joystick1 = JoystickWidget(this, 145, 7, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_left_arm"),  callback = ::joystickChange)
        joystick2 = JoystickWidget(this, 5, 7, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_right_arm"), true, callback = ::joystickChange)
        joystick3 = JoystickWidget(this, 145, 59, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_left_leg"), callback = ::joystickChange)
        joystick4 = JoystickWidget(this, 5, 59, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_right_leg"), true, callback = ::joystickChange)
        joystick5 = JoystickWidget(this, 5, 111, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_head"), true, callback = ::joystickChange)
        joystick6 = JoystickWidget(this, 145, 111, 48, 48, 14, 198, 0,
            MLCompatText.makeTranslatableText("gui.statuesclassic.joystick.creation_body"), callback = ::joystickChange)
        val randomizeButton = ButtonWidget(this, 4, 162, 190, 20, 0, 208, ::randomize, {MLCompatText.makeTranslatableText("gui.statuesclassic.randomize")})
        val formButton = object: ButtonWidget(this, 4, 184, 190, 20, 0, 208, ::form, {MLCompatText.makeTranslatableText("gui.statuesclassic.sculpt")}, { uuid != null }) {
            override fun getTooltip(): List<Text>?
            {
                return if (uuid == null) listOf(MLCompatText.makeTranslatableText("gui.statuesclassic.sculpt_invalid")) else null
            }
        }

        addWidgets(joystick1, joystick2, joystick3, joystick4, joystick5, joystick6, randomizeButton, formButton)

        block = MinecraftClient.getInstance().world?.getBlockState(handler.parentPos) ?: Blocks.STONE.defaultState
    }

    override fun init()
    {
        super.init()
        nameField = TextFieldWidget(textRenderer, x+59, y+139, 80, 12, MLCompatText.makeTranslatableText("gui.statuesclassic.player_name_field"))
        nameField.setEditable(true)
        nameField.setEditableColor(-1)
        nameField.setUneditableColor(-1)
        nameField.setDrawsBackground(false)
        addSelectableChild(nameField)
        setInitialFocus(nameField)
    }

    private fun joystickChange()
    {
        StatueHelper.applyJoystickAnglesToStatueData(data,
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
        if (uuid == null || trueName == null)
            return

        ClientPacketsOut.sendFormStatuePacket(trueName!!, uuid!!, data)
    }

    fun randomize()
    {
        for (widget in widgets)
            if (widget is JoystickWidget)
                widget.setPosition(Math.random().toFloat() * 2f -1f,Math.random().toFloat() * 2f -1f)
    }

    fun receiveProfile(queriedName: String, profile: GameProfile?)
    {
        if (queriedName.lowercase() == lastName.lowercase()) {
            if (profile != null) {
                uuid = profile.id
                trueName = profile.name
            }
        }
        else {
            //If name has changed while the lookup was processing, schedule another lookup
            lookupDelay = 0
        }
    }

    override fun handledScreenTick()
    {
        //Check if the input name has changed.
        if ((nameField.text.isNotEmpty() && nameField.text != lastName)) {
            //Schedule a lookup request
            if (lookupDelay == -1)
                lookupDelay = 0

            //Invalidate uuid and skin
            invalidateInfo()

            lastName = nameField.text
            if (lastName.isEmpty())
                invalidateInfo()
        }

        if (lookupDelay > -1) {
            lookupDelay++

            //Make sure not to spam lookup requests. The server-enforced limit is 1 per second, per player.
            if (lookupDelay >= 25) {
                lookupDelay = -1
                //This will eventually update the uuid and username in this screen.
                ClientPacketsOut.sendLookupUuidPacket(lastName)
            }
        }
    }

    private fun invalidateInfo()
    {
        uuid = null
        trueName = null
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

        //Draw text
        drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, MLCompatText.makeTranslatableText("gui.statuesclassic.username"), x+99, y+125, 16777215)
    }

    private fun drawModel(matrices: MatrixStack, size: Float, x: Double, y: Double, mouseX: Int, mouseY: Int, delta: Float)
    {
        //I have no idea what some of this is, it's just copied from InventoryScreen#drawEntity
        val skinData = if (uuid != null) SkinHandler.getCachedSkin(uuid!!) else null
        val skin = skinData?.getSkinOrDefault() ?: DefaultSkinHelper.getTexture()

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
            renderer.render(null, data, block, uuid, skinData?.slim ?: false, skin, delta, ourMatrix, immediate, fullbright, OverlayTexture.DEFAULT_UV)
        }
        immediate.draw()

        viewStack.pop()
        RenderSystem.applyModelViewMatrix()
        DiffuseLighting.enableGuiDepthLighting()

        snapshot.restore()
        matrices.pop()
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
            widget.onDragged(mouseX, mouseY, deltaX, deltaY, if (button == 0) BaseWidget.Button.LEFT else BaseWidget.Button.RIGHT)

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