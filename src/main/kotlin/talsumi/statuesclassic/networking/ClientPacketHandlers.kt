package talsumi.statuesclassic.networking

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import talsumi.statuesclassic.client.content.screen.StatueCreationScreen
import talsumi.statuesclassic.client.content.screen.StatueEquipmentScreen

object ClientPacketHandlers {

    fun register()
    {
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketsOut.send_statue_uuid, ::receiveStatueProfilePacket)
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketsOut.send_statue_gui_hands, ::receiveStatueHandsPacket)
    }

    fun receiveStatueProfilePacket(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val queriedName = buf.readString()
        val valid = buf.readBoolean()
        val username = if (valid) buf.readString() else null
        val uuid = if (valid) buf.readUuid() else null

        client.execute {
            val screen = client.currentScreen

            if (screen is StatueCreationScreen)
                screen.receiveProfile(queriedName, if (valid) GameProfile(uuid, username) else null)
        }
    }

    fun receiveStatueHandsPacket(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val left = buf.readFloat()
        val right = buf.readFloat()

        client.execute {
            val screen = client.currentScreen

            if (screen is StatueEquipmentScreen)
                screen.joysticksUpdatedFromServer(left, right)
        }
    }
}