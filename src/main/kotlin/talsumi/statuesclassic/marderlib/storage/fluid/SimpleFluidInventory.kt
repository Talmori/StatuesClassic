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

class SimpleFluidInventory(val tanks: Array<Int>): FluidInventory {

	val fluidsIn = Array<FluidStack>(tanks.size) {i -> FluidStack.EMPTY}

	override fun clear(tank: Int)
	{
		for (fluid in fluidsIn.withIndex())
			fluidsIn[fluid.index] = FluidStack.EMPTY
	}

	override fun tanks(): Int = tanks.size

	override fun tankSize(tank: Int): Int = tanks[tank]

	override fun isEmpty(tank: Int): Boolean = fluidsIn[tank].isEmpty()

	override fun getFluid(tank: Int): FluidStack = fluidsIn[tank]

	override fun setFluid(tank: Int, fluid: FluidStack) { fluidsIn[tank] = fluid }

	override fun removeFluid(tank: Int, amount: Int): FluidStack = fluidsIn[tank].split(amount)

	override fun removeFluid(tank: Int): FluidStack
	{
		val extract = fluidsIn[tank]
		fluidsIn[tank] = FluidStack.EMPTY
		return extract
	}

	override fun markDirty() = Unit
}