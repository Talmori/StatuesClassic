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

package talsumi.statuesclassic.marderlib.util

import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.lang.ref.WeakReference

class OptionalBlockEntity<T: BlockEntity>(world: World?, val pos: BlockPos, val validator: (BlockEntity) -> Boolean) {

    var world: WeakReference<World>? = null
    var be: WeakReference<T?>? = null

    init
    {
        if (world != null)
            this.world = WeakReference(world)
    }

    fun lateAddWorld(world: World)
    {
        this.world = WeakReference(world)
    }

    fun blockEntityExists(force: Boolean): Boolean
    {
        return get(force) != null
    }

    fun getBlockEntityModed(force: Boolean): T?
    {
        return get(force)
    }

    fun getBlockEntity(): T?
    {
        return get(false)
    }

    fun getBlockEntityForced(): T?
    {
        return get(true)
    }

    private fun validate()
    {
        if (world == null)
            throw IllegalStateException("World of $this is null! Ensure you call lateAddWorld before trying to access the stored BlockEntity")
        //If our BlockEntity is discarded or removed nullify [be]
        val got = be?.get()
        if (got == null || got.isRemoved)
            be = null
    }

    private fun get(force: Boolean): T?
    {
        validate()
        val world = world!!.get()
        if (be != null)
            return be!!.get()

        if (world != null) {
            val canLook = force || world.isChunkLoaded(pos)

            if (canLook) {
                val beInWorld = world.getBlockEntity(pos)
                if (beInWorld != null && validator.invoke(beInWorld)) {
                    be = WeakReference(beInWorld as T)
                    return beInWorld
                }
            }
        }

        return null
        /*
        validate()
        val world = world!!.get()
        if (be == null && world != null) {
            be = if (force || world.isChunkLoaded(pos)) {
                val beInWorld = world.getBlockEntity(pos)
                if (beInWorld != null && validator.invoke(beInWorld))
                     WeakReference(beInWorld as T)
                else null
            }
            else null
        }
        return be?.get()*/
    }

    override fun toString(): String = "OptionalBlockEntity(pos=$pos, world=$world, be=$be)"

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptionalBlockEntity<*>

        if (pos != other.pos) return false
        if (world?.get() != other.world?.get()) return false
        if (be?.get() != other.be?.get()) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = pos.hashCode()
        result = 31 * result + (world?.get()?.hashCode() ?: 0)
        result = 31 * result + (be?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun save(optional: OptionalBlockEntity<*>?): NbtCompound
        {
            val nbt = NbtCompound()
            if (optional != null)
                nbt.putIntArray("pos", arrayOf(optional.pos.x, optional.pos.y, optional.pos.z).toIntArray())
            return nbt
        }

        fun <T: BlockEntity> load(world: World?, nbt: NbtCompound, validator: (BlockEntity) -> Boolean): OptionalBlockEntity<T>?
        {
            val array = nbt.getIntArray("pos")
            return if (array.isNotEmpty())
                OptionalBlockEntity(world, BlockPos(array[0], array[1], array[2]), validator)
            else
                null
        }
    }
}