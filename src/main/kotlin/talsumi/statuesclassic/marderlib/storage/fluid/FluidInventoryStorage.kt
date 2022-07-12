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

package talsumi.statuesclassic.marderlib.storage.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.util.math.Direction
import java.lang.ref.WeakReference

/**
 * Like [InventoryStorage], but for a [FluidInventory]
 */
class FluidInventoryStorage(parts: MutableList<Part>?) : CombinedStorage<FluidVariant, FluidInventoryStorage.Part>(parts) {

	companion object {
		fun of(inventory: FluidInventory, side: Direction?): FluidInventoryStorage
		{
			val parts = ArrayList<Part>()

			if (inventory is SidedFluidInventory && side != null) {
				for (tank in inventory.getAvailableTanks(side))
					parts.add(Part(WeakReference(inventory), tank, side))
			}
			else {
				for (index in 0 until inventory.tanks())
					parts.add(Part(WeakReference(inventory), index, side))
			}

			return FluidInventoryStorage(parts)
		}
	}

	class Part(val inv: WeakReference<FluidInventory>, val tank: Int, val side: Direction?): SnapshotParticipant<ResourceAmount<FluidVariant>>(), Storage<FluidVariant>, StorageView<FluidVariant> {

		fun canInsert(variant: FluidVariant): Boolean
		{
			val inv = inv.get() ?: return false
			return inv.isValid(tank, variant) && (side == null || inv !is SidedFluidInventory || inv.canInsert(tank, variant, side))
		}

		fun canExtract(variant: FluidVariant): Boolean
		{
			val inv = inv.get() ?: return false
			return side == null || inv !is SidedFluidInventory || inv.canExtract(tank, variant, side)
		}

		override fun insert(fluid: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long
		{
			val inv = inv.get() ?: return 0
			val fluidIn = inv.getFluid(tank)

			StoragePreconditions.notBlankNotNegative(fluid, maxAmount)

			if (canInsert(fluid)) {
				val insertedAmount = maxAmount.toInt().coerceAtMost(inv.tankSize(tank) - inv.getFluid(tank).amount)
				if (insertedAmount > 0) {
					updateSnapshots(transaction)
					if (fluidIn.isEmpty()) {
						inv.setFluid(tank, FluidStack(fluid, insertedAmount))
					} else {
						fluidIn.amount += insertedAmount
						inv.setFluid(tank, fluidIn) //Ensure we call setFluid so the inventory saves its state
					}
				}

				return insertedAmount.toLong()
			}

			return 0
		}

		override fun extract(fluid: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long
		{
			val inv = inv.get() ?: return 0
			val fluidIn = inv.getFluid(tank)

			StoragePreconditions.notBlankNotNegative(fluid, maxAmount)

			if (canExtract(fluid)) {
				val extractedAmount = maxAmount.coerceAtMost(fluidIn.amount.toLong())
				if (extractedAmount > 0) {
					updateSnapshots(transaction)
					fluidIn.decrement(extractedAmount.toInt())
					inv.setFluid(tank, fluidIn) //Ensure we call setFluid so the inventory saves its state
					if (fluidIn.amount == 0)
						inv.setFluid(tank, FluidStack.EMPTY)
				}
				return extractedAmount
			}

			return 0
		}

		override fun iterator(transaction: TransactionContext?): MutableIterator<StorageView<FluidVariant>> = SingleViewIterator.create(this, transaction)

		override fun createSnapshot(): ResourceAmount<FluidVariant>
		{
			val fluid = inv.get()?.getFluid(tank) ?: FluidStack.EMPTY
			return ResourceAmount<FluidVariant>(fluid.fluid, fluid.amount.toLong())
		}

		override fun readSnapshot(snapshot: ResourceAmount<FluidVariant>)
		{
			inv.get()?.setFluid(tank, FluidStack(snapshot.resource, snapshot.amount.toInt()))
		}

		override fun isResourceBlank(): Boolean = inv.get()?.getFluid(tank)?.isEmpty() ?: true

		override fun getResource(): FluidVariant = inv.get()?.getFluid(tank)?.fluid ?: FluidVariant.blank()

		override fun getAmount(): Long = inv.get()?.getFluid(tank)?.amount?.toLong() ?: 0L

		override fun getCapacity(): Long = inv.get()?.tankSize(tank)?.toLong() ?: 0L
	}
}