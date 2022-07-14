package talsumi.statuesclassic.content.blockentity

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import talsumi.statues.networking.ServerPacketsOut
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.marderlib.storage.item.ItemStackHandler
import java.util.*

class StatueBE(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.statue, pos, state), IUpdatableBlockEntity {

    val inventory = ItemStackHandler(6, ::onContentsChanged)
    var playerUuid: UUID? = null
    var hasSetup = false
    var data: StatueData? = null
    var statueId = UUID.randomUUID()

    fun setup(uuid: UUID, data: StatueData)
    {
        playerUuid = uuid
        this.data = data
        hasSetup = true
        StatuesClassic.LOGGER.info("Created statue at $pos, for player $uuid")
        sendUpdatePacket()
    }

    fun sendUpdatePacket()
    {
        for (player in PlayerLookup.tracking(this))
            ServerPacketsOut.sendUpdateBlockEntityPacket(this, player)
    }

    private fun onContentsChanged()
    {
        sendUpdatePacket()
    }

    override fun writeUpdatePacket(buf: PacketByteBuf)
    {
        inventory.saveToByteBuf(buf)
        buf.writeUuid(playerUuid)
        buf.writeBoolean(data != null)
        data?.writePacket(buf)
    }

    override fun receiveUpdatePacket(buf: PacketByteBuf)
    {
        inventory.loadFromByteBuf(buf)
        playerUuid = buf.readUuid()
        if (buf.readBoolean())
            data = StatueData.fromPacket(buf)
    }

    override fun readNbt(nbt: NbtCompound)
    {
        super.readNbt(nbt)
        inventory.load(nbt.getCompound("inventory"))
        hasSetup = nbt.getBoolean("has_setup")
        if (hasSetup) {
            data = StatueData.load(nbt.getCompound("statue_data"))
            playerUuid = nbt.getUuid("player_uuid")
        }
    }

    override fun writeNbt(nbt: NbtCompound)
    {
        super.writeNbt(nbt)
        nbt.put("inventory", inventory.save())
        nbt.putBoolean("has_setup", hasSetup)
        if (data != null)
            nbt.put("statue_data", data!!.save())
        if (playerUuid != null)
            nbt.putUuid("player_uuid", playerUuid)
    }
}