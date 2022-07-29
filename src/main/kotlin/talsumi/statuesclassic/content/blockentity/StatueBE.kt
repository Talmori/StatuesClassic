package talsumi.statuesclassic.content.blockentity

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.entity.DummyPlayerEntity
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.marderlib.storage.item.ItemStackHandler
import talsumi.statuesclassic.networking.ServerPacketsOut
import java.util.*

class StatueBE(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.statue, pos, state), IUpdatableBlockEntity {

    val inventory = ItemStackHandler(6, ::onContentsChanged)

    var hasBeenSetup = false
    var playerUuid: UUID? = null
    var data: StatueData? = null
    var block: Block? = null

    var leftHandRotate = 0f
    var rightHandRotate = 0f

    var statueId = UUID.randomUUID()

    var clientFakePlayer: DummyPlayerEntity? = null

    fun setup(block: Block, uuid: UUID, data: StatueData)
    {
        playerUuid = uuid
        this.data = data
        this.block = block
        hasBeenSetup = true
        StatuesClassic.LOGGER.info("Created statue at $pos, for player $uuid, made of $block")
    }

    override fun setWorld(world: World)
    {
        super.setWorld(world)

        if (world.isClient)
            clientFakePlayer = DummyPlayerEntity(this, world, pos, 0f, GameProfile(null, "statuesclassic_fakeplayer"))
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
        buf.writeBoolean(hasBeenSetup)
        if (hasBeenSetup) {
            inventory.saveToByteBuf(buf)
            buf.writeUuid(playerUuid)
            buf.writeBoolean(data != null)
            data?.writePacket(buf)
            buf.writeString(block!!.registryEntry.registryKey().value.toString())
            buf.writeFloat(leftHandRotate)
            buf.writeFloat(rightHandRotate)
        }
    }

    override fun receiveUpdatePacket(buf: PacketByteBuf)
    {
        hasBeenSetup = buf.readBoolean()
        if (hasBeenSetup) {
            inventory.loadFromByteBuf(buf)
            playerUuid = buf.readUuid()
            if (buf.readBoolean())
                data = StatueData.fromPacket(buf)
            block = Registry.BLOCK.get(Identifier(buf.readString()))
            leftHandRotate = buf.readFloat()
            rightHandRotate = buf.readFloat()
        }
    }

    override fun readNbt(nbt: NbtCompound)
    {
        super.readNbt(nbt)
        inventory.load(nbt.getCompound("inventory"))
        hasBeenSetup = nbt.getBoolean("has_setup")
        if (hasBeenSetup) {
            data = StatueData.load(nbt.getCompound("statue_data"))
            playerUuid = nbt.getUuid("player_uuid")
            block = Registry.BLOCK.get(Identifier(nbt.getString("block")))
            leftHandRotate = nbt.getFloat("left_hand_rotate")
            rightHandRotate = nbt.getFloat("right_hand_rotate")
        }
    }

    override fun writeNbt(nbt: NbtCompound)
    {
        super.writeNbt(nbt)
        nbt.put("inventory", inventory.save())
        nbt.putBoolean("has_setup", hasBeenSetup)
        if (hasBeenSetup) {
            nbt.put("statue_data", data!!.save())
            nbt.putUuid("player_uuid", playerUuid!!)
            nbt.putString("block", block!!.registryEntry.registryKey().value.toString())
            nbt.putFloat("left_hand_rotate", leftHandRotate)
            nbt.putFloat("right_hand_rotate", rightHandRotate)
        }
    }
}