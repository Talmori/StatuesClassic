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

package talsumi.statuesclassic.core

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

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