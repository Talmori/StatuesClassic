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
            TranslatableText("gui.statuesclassic.joystick.equipment_right_hand"), true, callback = ::joystickChange)
        rightJoystick = JoystickWidget(117, 67, 51, 14, 14, 176, 0, this,
            TranslatableText("gui.statuesclassic.joystick.equipment_left_hand"), callback = ::joystickChange)

        addWidgets(leftJoystick, rightJoystick)

        leftJoystick.setPosition(screenHandler.statue?.rightHandRotate ?: 0f, 0f)
        rightJoystick.setPosition(screenHandler.statue?.leftHandRotate ?: 0f, 0f)

        setup = true
    }

    fun joysticksUpdatedFromServer(left: Float, right: Float)
    {
        println(left)
        println(right)
        leftJoystick.setPosition(left.coerceIn(-1f, 1f), 0f)
        rightJoystick.setPosition(right.coerceIn(-1f, 1f), 0f)
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