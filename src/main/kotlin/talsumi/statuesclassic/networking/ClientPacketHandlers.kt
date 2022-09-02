/*
 * MIT License
 *
 * Copyright (c) 2022 Talsumi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package talsumi.statuesclassic.networking

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
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
        val valid = buf.readBoolean()
        val username = if (valid) buf.readString() else null
        val uuid = if (valid) buf.readUuid() else null

        client.execute {
            val screen = client.currentScreen

            if (screen is StatueCreationScreen)
                screen.receiveProfile(if (valid) GameProfile(uuid, username) else null)
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