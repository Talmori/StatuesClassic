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

import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World
import java.lang.ref.WeakReference

class OptionalEntity<T: Entity>(world: World?, val id: Int, val validator: (Entity) -> Boolean) {

    var world: WeakReference<World>? = null
    var entity: WeakReference<T?>? = null

    init
    {
        if (world != null)
            this.world = WeakReference(world)
    }

    fun now(): OptionalEntity<T>
    {
        get()
        return this
    }

    fun lateAddWorld(world: World)
    {
        this.world = WeakReference(world)
    }

    fun entityExists(): Boolean
    {
        return get() != null
    }

    fun getEntity(): T?
    {
        return get()
    }

    private fun validate()
    {
        if (world == null)
            throw IllegalStateException("World of $this is null! Ensure you call lateAddWorld before trying to access the stored Entity")
        val got = entity?.get()
        if (got == null || got.isRemoved)
            entity = null
    }

    private fun get(): T?
    {
        validate()
        val world = world!!.get()
        if (entity == null && world != null) {
            entity = world.getEntityById(id)?.let { WeakReference(it as T) } ?: null
        }
        return entity?.get()
    }

    override fun toString(): String = "OptionalBlockEntity(id=$id, world=$world, be=$entity)"

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptionalEntity<*>

        if (id != other.id) return false
        if (world?.get() != other.world?.get()) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = id
        result = 31 * result + (world?.get()?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun save(optional: OptionalEntity<*>?): NbtCompound
        {
            val nbt = NbtCompound()
            if (optional != null)
                nbt.putInt("id", optional.id)
            return nbt
        }

        fun <T: Entity> load(world: World?, nbt: NbtCompound, validator: (Entity) -> Boolean): OptionalEntity<T>
        {
            return OptionalEntity(world, nbt.getInt("id"), validator)
        }
    }
}