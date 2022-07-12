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

package talsumi.statuesclassic.marderlib.screenhandler

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier
import talsumi.statuesclassic.marderlib.storage.SlotLimitations
import talsumi.statuesclassic.marderlib.storage.item.ItemStackHandler
import talsumi.statuesclassic.mixininterfaces.StatuesClassicPlayerListenerGrabber
import talsumi.statuesclassic.mixins.StatuesClassicScreenHandlerAccessor

abstract class EnhancedScreenHandler(type: ScreenHandlerType<*>?, syncId: Int) : ScreenHandler(type, syncId) {

	protected fun addPlayerInventory(playerInv: PlayerInventory, leftColumn: Int, topRow: Int)
	{
		addSlotBox(playerInv, 9, leftColumn, topRow, 9, 3, 18, 18)
		addSlotRange(playerInv, 0, leftColumn, topRow + 58, 9, 18)
	}

	public override fun addSlot(slot: Slot?): Slot = super.addSlot(slot)

	fun handleShiftClick(player: PlayerEntity, index: Int, inv: Inventory): ItemStack
	{
		var newStack = ItemStack.EMPTY
		val slot = slots[index]
		if (slot != null && slot.hasStack()) {
			val originalStack = slot.stack
			newStack = originalStack.copy()
			if (index < inv.size()) {
				if (!insertItem(originalStack, inv.size(), slots.size, true)) {
					return ItemStack.EMPTY
				}
			} else if (!insertItem(originalStack, 0, inv.size(), false)) {
				return ItemStack.EMPTY
			}
			if (originalStack.isEmpty) {
				slot.stack = ItemStack.EMPTY
			} else {
				slot.markDirty()
			}
		}

		return newStack!!
	}

	fun addSlot(inv: Inventory, index: Int, x: Int, y: Int)
	{
		addSlot(BetterSlot(inv, index, x, y))
	}

	fun addSlotRange(inv: Inventory, index: Int, x: Int, y: Int, amount: Int, separation: Int): Int
	{
		var x = x
		var index = index
		for (pos in 0 until amount) {
			addSlot(BetterSlot(inv, index++, x, y))
			x += separation
		}

		return index
	}

	fun addSlotBox(inv: Inventory, index: Int, x: Int, y: Int, columns: Int, rows: Int, separationX: Int, separationY: Int): Int
	{
		var index = index
		var y = y
		for (yPos in 0 until rows) {
			index = addSlotRange(inv, index, x, y, columns, separationX)
			y += separationY
		}

		return index
	}

	fun getListeners(): List<StatuesClassicPlayerListenerGrabber>
	{
		return (this as StatuesClassicScreenHandlerAccessor).statuesclassic_getListeners() as MutableList<StatuesClassicPlayerListenerGrabber>
	}

	/**
	 * Allows general receiving of packets in any way wanted. Packet handling must still call this method somehow.
	 */
	open fun receivePacket(channel: Identifier, buf: PacketByteBuf)
	{

	}

	open fun onListenerAdd(listener: StatuesClassicPlayerListenerGrabber)
	{

	}

	override fun addListener(listener: ScreenHandlerListener?)
	{
		onListenerAdd(listener as StatuesClassicPlayerListenerGrabber)
		super.addListener(listener)
	}

	class BetterSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {

		val limits: SlotLimitations<ItemVariant>? = if (inventory is ItemStackHandler) inventory.cachedGetSlotLimitations(index) else null

		override fun canInsert(stack: ItemStack): Boolean = limits == null || (limits.canInsert && limits.allowed?.invoke(
			ItemVariant.of(stack)) ?: true)
	}
}