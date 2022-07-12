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

package talsumi.statuesclassic.marderlib.storage.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import talsumi.statuesclassic.marderlib.storage.SlotLimitations

class ItemStackHandler(val slots: Int, val callback: (() -> Unit)?, var sidedness: Array<SlotLimitations<ItemVariant>>? = null, var sideRemapper: ((Direction) -> Direction)? = null): SidedInventory {

	private val items = DefaultedList.ofSize(slots, ItemStack.EMPTY)
	private val sidedCache = HashMap<Int, SlotLimitations<ItemVariant>>()

	fun updateSideness(sidedness: Array<SlotLimitations<ItemVariant>>?)
	{
		this.sidedness = sidedness
		sidedCache.clear()
	}

	fun getItems(): List<ItemStack> = listOf(*items.toTypedArray())

	fun save(): NbtCompound
	{
		val nbt = NbtCompound()
		Inventories.writeNbt(nbt, items)
		return nbt
	}

	fun load(nbt: NbtCompound) = Inventories.readNbt(nbt, items)

	fun rawGet(slot: Int): ItemStack = items.get(slot)

	fun rawSet(slot: Int, item: ItemStack)
	{
		items.set(slot, item)
		markDirty()
	}

	fun cachedGetSlotLimitations(slot: Int): SlotLimitations<ItemVariant>?
	{
		if (sidedness == null)
			return null

		if (sidedCache[slot] != null)
			return sidedCache[slot]

		for (limits in sidedness!!)
			if (limits.slotFrom <= slot && slot <= limits.slotTo) {
				sidedCache[slot] = limits
				return limits
			}

		return null
	}

	override fun clear()
	{
		items.clear()
		markDirty()
	}

	override fun size(): Int = slots

	override fun isEmpty(): Boolean
	{
		items.forEach { if (!it.isEmpty) return false }
		return true
	}

	override fun getStack(slot: Int): ItemStack = items[slot]

	override fun removeStack(slot: Int, amount: Int): ItemStack
	{
		val extract = Inventories.splitStack(items, slot, amount)
		if (!extract.isEmpty)
			markDirty()
		return extract
	}

	override fun removeStack(slot: Int): ItemStack
	{
		markDirty()
		return Inventories.removeStack(items, slot)
	}

	override fun setStack(slot: Int, stack: ItemStack)
	{
		items[slot] = stack
		if (stack.count > maxCountPerStack)
			stack.count = maxCountPerStack
		markDirty()
	}

	override fun markDirty()
	{
		callback?.invoke()
	}

	override fun canPlayerUse(player: PlayerEntity?): Boolean = true

	override fun isValid(slot: Int, stack: ItemStack): Boolean
	{
		val limits = cachedGetSlotLimitations(slot) ?: return true
		return limits.canInsert && limits.allowed?.invoke(ItemVariant.of(stack)) ?: true
	}

	override fun getAvailableSlots(side: Direction?): IntArray
	{
		val side = if (side != null) sideRemapper?.invoke(side) ?: side else null
		val list = ArrayList<Int>()
		for (slot in 0 until items.size)
			if (sidedness == null || slotAllowed(slot, side))
				list.add(slot)

		return list.toIntArray()
	}

	private fun slotAllowed(slot: Int, side: Direction?): Boolean
	{
		for (limits in sidedness!!) {
			if (limits.slotFrom <= slot && slot <= limits.slotTo)
				if ((side == null || limits.sides.isEmpty() || limits.sides.contains(side)))
					return true
		}

		return false
	}

	override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean
	{
		val dir = if (dir != null) sideRemapper?.invoke(dir) ?: dir else null
		val limits = cachedGetSlotLimitations(slot) ?: return true
		return (limits.allowed?.invoke(ItemVariant.of(stack)) ?: true && (dir == null || limits.sides.isEmpty() || limits.sides.contains(dir)) && limits.canInsert)
	}

	override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean
	{
		val dir = if (dir != null) sideRemapper?.invoke(dir) ?: dir else null
		val limits = cachedGetSlotLimitations(slot) ?: return true
		return ((dir == null || limits.sides.contains(dir) || limits.sides.isEmpty()) && limits.canExtract)
	}

	fun saveToByteBuf(buf: PacketByteBuf)
	{
		buf.writeInt(size())
		for (item in items) {
			val nbt = NbtCompound()
			item.writeNbt(nbt)
			buf.writeNbt(nbt)
		}
	}

	fun loadFromByteBuf(buf: PacketByteBuf)
	{
		val length = buf.readInt().coerceAtMost(size())
		for (index in 0 until length)
			items[index] = ItemStack.fromNbt(buf.readNbt())
	}
}