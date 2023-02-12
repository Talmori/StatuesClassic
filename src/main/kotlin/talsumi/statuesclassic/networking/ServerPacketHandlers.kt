package talsumi.statuesclassic.networking

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.registry.Registry
import talsumi.marderlib.mixininterfaces.MarderLibPlayerListenerGrabber
import talsumi.marderlib.mixins.MarderLibScreenHandlerAccessor
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.core.UUIDLookups

object ServerPacketHandlers {

    fun register()
    {
        ServerPlayNetworking.registerGlobalReceiver(ClientPacketsOut.form_statue, ::receiveFormStatuePacket)
        ServerPlayNetworking.registerGlobalReceiver(ClientPacketsOut.update_statue_hands, ::receiveUpdateStatueHandsPacket)
        ServerPlayNetworking.registerGlobalReceiver(ClientPacketsOut.lookup_uuid, ::receiveLookupUuidPacket)
    }

    private fun receiveFormStatuePacket(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val name = buf.readString()
        val uuid = buf.readUuid()
        val data = StatueData.fromPacket(buf)

        if (UUIDLookups.rawGet(name)?.id ?: uuid == uuid) {//Sanity check to ensure username matches uuid
            server.execute {
                if (player.currentScreenHandler is StatueCreationScreenHandler)
                    (player.currentScreenHandler as StatueCreationScreenHandler).form(name, uuid, data)
            }
        }
    }

    private fun receiveUpdateStatueHandsPacket(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val left = buf.readFloat().coerceIn(-1f, 1f)
        val right = buf.readFloat().coerceIn(-1f, 1f)

        server.execute {
            val screen = player.currentScreenHandler
            if (screen is StatueEquipmentScreenHandler) {

                //Update statue hands
                screen.updateHands(left, right)

                //Update joystick positions for every listening player
                for (listener in (screen as MarderLibScreenHandlerAccessor).marderlib_getListeners()) {
                    val listeningPlayer = (listener as MarderLibPlayerListenerGrabber).marderlib_getOwningPlayer()

                    if (listeningPlayer != player)
                        ServerPacketsOut.sendStatueHandsPacket(left, right, listeningPlayer)
                }

            }
        }
    }

    private fun receiveLookupUuidPacket(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val username = buf.readString()

        server.execute {
            if (player.currentScreenHandler is StatueCreationScreenHandler) {
                UUIDLookups.lookupProfileFromClient(player, server, username, whenFound = {
                    ServerPacketsOut.sendStatueProfilePacket(username, it, player)
                },
                whenFailed = {
                    ServerPacketsOut.sendStatueProfilePacket(username,null, player)
                })
            }
        }
    }
}