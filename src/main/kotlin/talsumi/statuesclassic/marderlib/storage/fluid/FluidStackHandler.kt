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
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.Direction
import talsumi.statuesclassic.marderlib.storage.SlotLimitations

class FluidStackHandler(val tankCount: Int, vararg tankSizes: Int, val callback: () -> Unit, var sidedness: Array<SlotLimitations<FluidVariant>>? = null, var sideRemapper: ((Direction) -> Direction)? = null):
	SidedFluidInventory {

	private val tanks = Array<Tank>(tankCount) { index -> (FluidStackHandler::Tank)(tankSizes[index], FluidStack.EMPTY)}
	private val sidedCache = HashMap<Int, SlotLimitations<FluidVariant>>()

	fun cachedGetTankLimitations(slot: Int): SlotLimitations<FluidVariant>?
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

	fun updateSideness(sidedness: Array<SlotLimitations<FluidVariant>>?)
	{
		this.sidedness = sidedness
		sidedCache.clear()
	}

	fun save(): NbtCompound
	{
		val nbt = NbtCompound()
		val tankList = NbtList()
		for (tank in tanks)
			tankList.add(tank.save())
		nbt.put("tanks", tankList)

		return nbt
	}

	fun load(nbt: NbtCompound)
	{
		val tankList = nbt.getList("tanks", NbtElement.COMPOUND_TYPE.toInt())
		for (tank in 0 until tankList.size)
			tanks[tank].load(tankList.getCompound(tank))
	}

	override fun tankSize(tank: Int): Int = tanks[tank].size

	override fun clear(tank: Int)
	{
		tanks[tank].fluidIn = FluidStack.EMPTY
	}

	override fun tanks(): Int = tanks.size

	override fun isEmpty(tank: Int): Boolean = tanks[tank].fluidIn.isEmpty()

	override fun getFluid(tank: Int): FluidStack = tanks[tank].fluidIn

	override fun setFluid(tank: Int, fluid: FluidStack)
	{
		tanks[tank].fluidIn = fluid
		markDirty()
	}

	override fun removeFluid(tank: Int, amount: Int): FluidStack
	{
		if (cachedGetTankLimitations(tank)?.canExtract != true)
			return FluidStack.EMPTY
		val extract = tanks[tank].fluidIn.split(amount)
		markDirty()
		return extract
	}

	override fun removeFluid(tank: Int): FluidStack
	{
		return if (cachedGetTankLimitations(tank)?.canExtract != true) {
			FluidStack.EMPTY
		} else {
			markDirty()
			val extract = tanks[tank].fluidIn
			tanks[tank].fluidIn = FluidStack.EMPTY
			extract
		}
	}

	override fun isValid(tank: Int, fluid: FluidVariant): Boolean
	{
		return (tanks[tank].fluidIn.isEmpty() || tanks[tank].fluidIn.fluid == fluid) && cachedGetTankLimitations(tank)?.allowed?.invoke(fluid) ?: true
	}

	override fun markDirty() = callback?.invoke()

	override fun getAvailableTanks(side: Direction?): Array<Int>
	{
		val dir = if (side != null) sideRemapper?.invoke(side) ?: side else null
		val list = ArrayList<Int>()
		for (tank in tanks.indices)
			if (sidedness == null || tankAllowed(tank, side))
				list.add(tank)

		return list.toTypedArray()
	}

	private fun tankAllowed(tank: Int, side: Direction?): Boolean
	{
		for (limits in sidedness!!) {
			if (limits.slotFrom <= tank && tank <= limits.slotTo)
				if ((side == null || limits.sides.isEmpty() || limits.sides.contains(side)))
					return true
		}

		return false
	}

	override fun canInsert(tank: Int, fluid: FluidVariant, side: Direction?): Boolean
	{
		val side = if (side != null) sideRemapper?.invoke(side) ?: side else null
		val limits = cachedGetTankLimitations(tank) ?: return true
		return (limits.allowed?.invoke(fluid) ?: true && (side == null || limits.sides.isEmpty() || limits.sides.contains(side)) && limits.canInsert)
	}

	override fun canExtract(tank: Int, fluid: FluidVariant, side: Direction?): Boolean
	{
		val side = if (side != null) sideRemapper?.invoke(side) ?: side else null
		val limits = cachedGetTankLimitations(tank) ?: return true
		return ((side == null || limits.sides.contains(side) || limits.sides.isEmpty()) && limits.canExtract)
	}

	class Tank(val size: Int, var fluidIn: FluidStack = FluidStack.EMPTY) {
		fun save(): NbtCompound
		{
			val nbt = NbtCompound()

			nbt.put("fluid", fluidIn.save())

			return nbt
		}

		fun load(nbt: NbtCompound)
		{
			fluidIn = FluidStack.fromNbt(nbt.getCompound("fluid"))
		}
	}
}