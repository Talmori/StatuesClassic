package talsumi.statuesclassic.content

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler

object ModScreenHandlers {

    val statue_equipment_screen = ScreenHandlerRegistry.registerExtended(Identifier(StatuesClassic.MODID, "statue_equipment_screen")) { id, inv, buf -> StatueEquipmentScreenHandler(id, inv, buf)}
    val statue_creation_screen = ScreenHandlerRegistry.registerExtended(Identifier(StatuesClassic.MODID, "statue_creation_screen")) { id, inv, buf -> StatueCreationScreenHandler(id, inv, buf) }

    fun wake() = Unit
}