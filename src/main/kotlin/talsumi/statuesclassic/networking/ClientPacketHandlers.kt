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

package talsumi.statues.networking

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.client.content.screen.StatueCreationScreen
import talsumi.statuesclassic.content.blockentity.IUpdatableBlockEntity

object ClientPacketHandlers {

    fun register()
    {
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketsOut.update_block_entity, ::receiveUpdateBlockEntityPacket)
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketsOut.send_statue_uuid, ::receiveStatueUuidPacket)
    }

    fun receiveUpdateBlockEntityPacket(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val pos = buf.readBlockPos()
        val type = buf.readIdentifier()

        buf.retain()
        client.execute {
            val be = client.world?.getBlockEntity(pos, Registry.BLOCK_ENTITY_TYPE.get(type))?.orElse(null) ?: return@execute

            if (be is IUpdatableBlockEntity)
                be.receiveUpdatePacket(buf)

            buf.release()
        }
    }

    fun receiveStatueUuidPacket(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender)
    {
        val username = buf.readString()
        val uuid = buf.readUuid()

        client.execute {
            val screen = client.currentScreen

            if (screen is StatueCreationScreen)
                screen.receiveUuid(username, uuid)
        }
    }
}