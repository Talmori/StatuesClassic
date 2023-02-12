package talsumi.statuesclassic.client.content

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import talsumi.statuesclassic.client.content.screen.StatueCreationScreen
import talsumi.statuesclassic.client.content.screen.StatueEquipmentScreen
import talsumi.statuesclassic.content.ModScreenHandlers

object ModClientScreens {
    val statue_equipment_screen = ScreenRegistry.register(ModScreenHandlers.statue_equipment_screen, ::StatueEquipmentScreen)
    val statue_creation_screen = ScreenRegistry.register(ModScreenHandlers.statue_creation_screen, ::StatueCreationScreen)

    fun wake() = Unit
}