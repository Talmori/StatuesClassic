package talsumi.statuesclassic.networking

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.StatuesClassic
import java.util.*

object ServerPacketsOut {

    val send_statue_uuid = Identifier(StatuesClassic.MODID, "send_statue_uuid")
    val send_statue_gui_hands = Identifier(StatuesClassic.MODID, "send_statue_gui_hands")

    fun sendStatueProfilePacket(queriedName: String, profile: GameProfile?, player: ServerPlayerEntity)
    {
        val buf = PacketByteBufs.create()
        buf.writeString(queriedName)
        buf.writeBoolean(profile != null)
        if (profile != null) {
            buf.writeString(profile.name)
            buf.writeUuid(profile.id)
        }
        ServerPlayNetworking.send(player, send_statue_uuid, buf)
    }

    fun sendStatueHandsPacket(left: Float, right: Float, player: ServerPlayerEntity)
    {
        val buf = PacketByteBufs.create()
        buf.writeFloat(left)
        buf.writeFloat(right)

        ServerPlayNetworking.send(player, send_statue_gui_hands, buf)
    }
}