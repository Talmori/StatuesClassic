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