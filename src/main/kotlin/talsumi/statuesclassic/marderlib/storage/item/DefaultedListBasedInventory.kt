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

package talsumi.statuesclassic.marderlib.storage.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

class DefaultedListBasedInventory(val items: DefaultedList<ItemStack>, val callback: (() -> Unit)?): Inventory {

    override fun clear() = items.clear()

    override fun size(): Int = items.size

    override fun isEmpty(): Boolean
    {
        items.forEach { if (!it.isEmpty) return false }
        return true
    }

    override fun getStack(slot: Int): ItemStack = items.get(slot)

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
}