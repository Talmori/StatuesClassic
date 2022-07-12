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
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry


class FluidStack(val fluid: FluidVariant, var amount: Int) {

	constructor(fluid: Fluid, amount: Int): this(FluidVariant.of(fluid), amount)

	fun isEmpty(): Boolean = this == EMPTY || this.amount == 0 || this.fluid == FluidVariant.blank()

	companion object {
		val EMPTY = FluidStack(FluidVariant.blank(), 0)

		fun fromNbt(nbt: NbtCompound): FluidStack
		{
			val fluidIn = Registry.FLUID[Identifier(nbt.getString("fluid"))]
			val data = nbt.getCompound("nbt")
			return FluidStack(FluidVariant.of(fluidIn, if (data.isEmpty) null else data), nbt.getInt("amount"))
		}

		fun fromPacket(buf: PacketByteBuf): FluidStack
		{
			val isEmpty = buf.readBoolean()

			if (!isEmpty) {
				val fluid = Registry.FLUID.get(Identifier(buf.readString()))
				val amount = buf.readInt()
				val nbt = buf.readNbt()
				return FluidStack(FluidVariant.of(fluid, nbt), amount)
			}

			return FluidStack.EMPTY
		}
	}

	fun save(): NbtCompound
	{
		val nbt = NbtCompound()

		nbt.putString("fluid", fluid.fluid.registryEntry.registryKey().value.toString())
		nbt.put("nbt", fluid.nbt ?: NbtCompound())
		nbt.putInt("amount", amount)

		return nbt
	}

	fun encodePacket(buf: PacketByteBuf)
	{
		buf.writeBoolean(isEmpty())

		if (!isEmpty()) {
			buf.writeString(fluid.fluid.registryEntry.registryKey().value.toString()) //Fluid Identifier
			buf.writeInt(amount)
			buf.writeNbt(fluid.nbt ?: NbtCompound())
		}
	}

	fun split(amount: Int): FluidStack
	{
		if (isEmpty()) return EMPTY

		val i = Math.min(amount, this.amount)
		val stack: FluidStack = this.copy()
		stack.amount = i
		this.decrement(i)
		return stack
	}

	fun getFluid(): Fluid = fluid.fluid

	fun increment(amount: Int)
	{
		this.amount+=amount
	}

	fun decrement(amount: Int)
	{
		this.amount-=amount
	}

	fun copy(): FluidStack = if (isEmpty()) EMPTY else FluidStack(fluid, amount)

	//Thanks IDEA!
	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FluidStack

		if (fluid != other.fluid) return false
		if (amount != other.amount) return false

		return true
	}

	override fun hashCode(): Int
	{
		var result = fluid.hashCode()
		result = 31 * result + amount
		return result
	}

	override fun toString(): String
	{
		return "FluidStack(fluid=$fluid, amount=$amount)"
	}
}