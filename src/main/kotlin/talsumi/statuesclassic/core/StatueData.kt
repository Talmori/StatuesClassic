package talsumi.statuesclassic.core

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.*

class StatueData(var leftArmRaise: Float,
                 var leftArmRotate: Float,
                 var rightArmRaise: Float,
                 var rightArmRotate: Float,
                 var leftLegRaise: Float,
                 var leftLegRotate: Float,
                 var rightLegRaise: Float,
                 var rightLegRotate: Float,
                 var headRaise: Float,
                 var headRotate: Float,
                 var masterRaise: Float,
                 var masterRotate: Float) {

    companion object {
        fun fromPacket(buf: PacketByteBuf): StatueData
        {
            return StatueData(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
            )
        }

        fun load(nbt: NbtCompound): StatueData
        {
            return StatueData(
                leftArmRaise = nbt.getFloat("leftArmRaise"),
                leftArmRotate = nbt.getFloat("leftArmRotate"),
                rightArmRaise = nbt.getFloat("rightArmRaise"),
                rightArmRotate = nbt.getFloat("rightArmRotate"),
                leftLegRaise = nbt.getFloat("leftLegRaise"),
                leftLegRotate = nbt.getFloat("leftLegRotate"),
                rightLegRaise = nbt.getFloat("rightLegRaise"),
                rightLegRotate = nbt.getFloat("rightLegRotate"),
                headRaise = nbt.getFloat("headRaise"),
                headRotate = nbt.getFloat("headRotate"),
                masterRaise = nbt.getFloat("masterRaise"),
                masterRotate = nbt.getFloat("masterRotate"))
        }
    }

    fun writePacket(buf: PacketByteBuf)
    {
        buf.writeFloat(leftArmRaise)
        buf.writeFloat(leftArmRotate)
        buf.writeFloat(rightArmRaise)
        buf.writeFloat(rightArmRotate)
        buf.writeFloat(leftLegRaise)
        buf.writeFloat(leftLegRotate)
        buf.writeFloat(rightLegRaise)
        buf.writeFloat(rightLegRotate)
        buf.writeFloat(headRaise)
        buf.writeFloat(headRotate)
        buf.writeFloat(masterRaise)
        buf.writeFloat(masterRotate)
    }

    fun save(): NbtCompound
    {
        val nbt = NbtCompound()
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
        return nbt
    }
}