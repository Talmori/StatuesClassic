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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.core.StatueData
import java.util.*

object ClientPacketsOut {

    val request_block_entity_update = Identifier(StatuesClassic.MODID, "request_block_entity_update")
    val form_statue = Identifier(StatuesClassic.MODID, "form_statue")
    val update_statue_hands = Identifier(StatuesClassic.MODID, "update_statue_hands")
    val lookup_uuid = Identifier(StatuesClassic.MODID, "lookup_uuid")


    fun sendFormStatuePacket(name: String, uuid: UUID, data: StatueData)
    {
        val buf = PacketByteBufs.create()
        buf.writeString(name)
        buf.writeUuid(uuid)
        data.writePacket(buf)
        ClientPlayNetworking.send(form_statue, buf)
    }

    fun sendUpdateStatueHandsPacket(left: Float, right: Float)
    {
        val buf = PacketByteBufs.create()
        buf.writeFloat(left)
        buf.writeFloat(right)
        ClientPlayNetworking.send(update_statue_hands, buf)
    }

    fun sendLookupUuidPacket(username: String)
    {
        val buf = PacketByteBufs.create()
        buf.writeString(username)
        ClientPlayNetworking.send(lookup_uuid, buf)
    }
}