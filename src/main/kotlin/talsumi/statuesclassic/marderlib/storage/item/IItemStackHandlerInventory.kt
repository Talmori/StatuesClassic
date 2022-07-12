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

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

/**
 * An implementation of [SidedInventory] wrapped around an [ItemStackHandler]
 */
interface IItemStackHandlerInventory: SidedInventory {

	fun getInventory(): ItemStackHandler

	override fun clear() = getInventory().clear()

	override fun size(): Int = getInventory().size()

	override fun isEmpty(): Boolean = getInventory().isEmpty

	override fun getStack(slot: Int): ItemStack = getInventory().getStack(slot)

	override fun removeStack(slot: Int, amount: Int): ItemStack = getInventory().removeStack(slot, amount)

	override fun removeStack(slot: Int): ItemStack = getInventory().removeStack(slot)

	override fun setStack(slot: Int, stack: ItemStack) = getInventory().setStack(slot, stack)

	override fun markDirty() = getInventory().markDirty()

	override fun canPlayerUse(player: PlayerEntity?): Boolean = getInventory().canPlayerUse(player)

	override fun getAvailableSlots(side: Direction?): IntArray = getInventory().getAvailableSlots(side)

	override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean = getInventory().canInsert(slot, stack, dir)

	override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean = getInventory().canExtract(slot, stack, dir)
}