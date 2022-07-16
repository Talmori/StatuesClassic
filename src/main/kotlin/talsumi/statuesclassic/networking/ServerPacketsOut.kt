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

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.blockentity.IUpdatableBlockEntity
import java.util.*

object ServerPacketsOut {

    val update_block_entity = Identifier(StatuesClassic.MODID, "update_block_entity")
    val send_statue_uuid = Identifier(StatuesClassic.MODID, "send_statue_uuid")

    fun <T> sendUpdateBlockEntityPacket(be: T, player: ServerPlayerEntity) where T: IUpdatableBlockEntity, T: BlockEntity
    {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(be.pos)
        buf.writeIdentifier(Registry.BLOCK_ENTITY_TYPE.getId(be.type))
        be.writeUpdatePacket(buf)
        ServerPlayNetworking.send(player, update_block_entity, buf)
    }

    fun sendStatueUuidPacket(username: String, uuid: UUID, player: ServerPlayerEntity)
    {
        val buf = PacketByteBufs.create()
        buf.writeString(username)
        buf.writeUuid(uuid)
        ServerPlayNetworking.send(player, send_statue_uuid, buf)
    }
}