package talsumi.statuesclassic.content.blockentity

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import talsumi.statues.networking.ServerPacketsOut
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.marderlib.storage.item.ItemStackHandler

class StatueBE(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.statue, pos, state), IUpdatableBlockEntity {

    val inventory = ItemStackHandler(6, ::onContentsChanged)
    var inSetup = true
    var leftArmRaise = 0f
    var leftArmRotate = 0f
    var rightArmRaise = 0f
    var rightArmRotate = 0f
    var leftLegRaise = 0f
    var leftLegRotate = 0f
    var rightLegRaise = 0f
    var rightLegRotate = 0f
    var headRaise = 0f
    var headRotate = 0f
    var masterRaise = 0f
    var masterRotate = 0f

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
    }

    override fun receiveUpdatePacket(buf: PacketByteBuf)
    {
        inventory.loadFromByteBuf(buf)
    }

    override fun readNbt(nbt: NbtCompound)
    {
        super.readNbt(nbt)
        inventory.load(nbt.getCompound("inventory"))
        inSetup = nbt.getBoolean("in_setup")
        leftArmRaise = nbt.getFloat("leftArmRaise")
        leftArmRotate = nbt.getFloat("leftArmRotate")
        rightArmRaise = nbt.getFloat("rightArmRaise")
        rightArmRotate = nbt.getFloat("rightArmRotate")
        leftLegRaise = nbt.getFloat("leftLegRaise")
        leftLegRotate = nbt.getFloat("leftLegRotate")
        rightLegRaise = nbt.getFloat("rightLegRaise")
        rightLegRotate = nbt.getFloat("rightLegRotate")
        headRaise = nbt.getFloat("headRaise")
        headRotate = nbt.getFloat("headRotate")
        masterRaise = nbt.getFloat("masterRaise")
        masterRotate = nbt.getFloat("masterRotate")
    }

    override fun writeNbt(nbt: NbtCompound)
    {
        super.writeNbt(nbt)
        nbt.put("inventory", inventory.save())
        nbt.putBoolean("in_setup", inSetup)
        nbt.putFloat("leftArmRaise", leftArmRaise)
        nbt.putFloat("leftArmRotate", leftArmRotate)
        nbt.putFloat("rightArmRaise", rightArmRaise)
        nbt.putFloat("rightArmRotate", rightArmRotate)
        nbt.putFloat("leftLegRaise", leftLegRaise)
        nbt.putFloat("leftLegRotate", leftLegRotate)
        nbt.putFloat("rightLegRaise", rightLegRaise)
        nbt.putFloat("rightLegRotate", rightLegRotate)
        nbt.putFloat("headRaise", headRaise)
        nbt.putFloat("headRotate", headRotate)
        nbt.putFloat("masterRaise", masterRaise)
        nbt.putFloat("masterRotate", masterRotate)
    }
}