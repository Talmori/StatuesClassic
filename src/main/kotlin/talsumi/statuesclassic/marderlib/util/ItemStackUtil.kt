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

package talsumi.statuesclassic.marderlib.util

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.item.ItemStack
import net.minecraft.util.DyeColor
import talsumi.statuesclassic.marderlib.storage.fluid.FluidStackHandler
import talsumi.statuesclassic.marderlib.storage.item.ItemStackHandler

object ItemStackUtil {

	fun canAddToStack(base: ItemStack, new: ItemStack): Boolean = base.isEmpty || (base.item == new.item && base.damage == new.damage && base.nbt == new.nbt && base.count + new.count <= base.maxCount)

	fun combineStacks(first: ItemStack, second: ItemStack): ItemStack
	{
		if (first.isEmpty)
			return second

		return ItemStack(first.item, first.count + second.count)
	}

	/**
	 * Fills or empties a tank in a [FluidStackHandler] from two item slots in an [ItemStackHandler] inventory.
	 */
	/*fun bucketFiller(inv: ItemStackHandler, fInv: FluidStackHandler, inSlot: Int, outSlot: Int, tank: Int, canFillBuckets: Boolean = true, canEmptyBuckets: Boolean = true)
	{
		val inItem = inv.rawGet(inSlot)
		val outItem = inv.rawGet(outSlot)
		val fluidIn = fInv.getFluid(tank)

		if (canFillBuckets) {
			if (inItem.item == Items.BUCKET && fluidIn.amount >= 1000) {
				val filledBucket = ItemStack(fluidIn.getFluid().bucketItem)

				if (canAddToStack(outItem, filledBucket)) {
					if (outItem.isEmpty)
						inv.rawSet(outSlot, filledBucket)
					else
						outItem.increment(1)

					inItem.decrement(1)
					fluidIn.decrement(1000)
					return
				}
			}
		}
		if (canEmptyBuckets) {
			val fluidInBucket = if (inItem.item is RailExpansionBucketItemFluidAccessor) (inItem.item as RailExpansionBucketItemFluidAccessor).railexpansion_getFluid() else Fluids.EMPTY

			if (fluidInBucket != Fluids.EMPTY && ((inItem.item is BucketItem && fluidIn.isEmpty()) || inItem.item == fluidIn.getFluid().bucketItem) && fluidIn.amount+1000 <= fInv.tankSize(tank)) {
				val emptyBucket = ItemStack(Items.BUCKET)

				if (ItemStackUtil.canAddToStack(outItem, emptyBucket)) {
					inItem.decrement(1)

					if (outItem.isEmpty)
						inv.rawSet(outSlot, emptyBucket)
					else
						outItem.increment(1)

					if (fluidIn.isEmpty())
						fInv.setFluid(tank, FluidStack(fluidInBucket, 1000))
					else
						fluidIn.increment(1000)
					return
				}
			}
		}
	}*/

	fun getDyeColour(item: ItemStack): DyeColor?
	{
		return when {
			item.isIn(ConventionalItemTags.WHITE_DYES) -> DyeColor.WHITE
			item.isIn(ConventionalItemTags.ORANGE_DYES) -> DyeColor.ORANGE
			item.isIn(ConventionalItemTags.MAGENTA_DYES) -> DyeColor.MAGENTA
			item.isIn(ConventionalItemTags.LIGHT_BLUE_DYES) -> DyeColor.LIGHT_BLUE
			item.isIn(ConventionalItemTags.YELLOW_DYES) -> DyeColor.YELLOW
			item.isIn(ConventionalItemTags.LIME_DYES) -> DyeColor.LIME
			item.isIn(ConventionalItemTags.PINK_DYES) -> DyeColor.PINK
			item.isIn(ConventionalItemTags.GRAY_DYES) -> DyeColor.GRAY
			item.isIn(ConventionalItemTags.LIGHT_GRAY_DYES) -> DyeColor.LIGHT_GRAY
			item.isIn(ConventionalItemTags.CYAN_DYES) -> DyeColor.CYAN
			item.isIn(ConventionalItemTags.PURPLE_DYES) -> DyeColor.PURPLE
			item.isIn(ConventionalItemTags.BLUE_DYES) -> DyeColor.BLUE
			item.isIn(ConventionalItemTags.BROWN_DYES) -> DyeColor.BROWN
			item.isIn(ConventionalItemTags.GREEN_DYES) -> DyeColor.GREEN
			item.isIn(ConventionalItemTags.RED_DYES) -> DyeColor.RED
			item.isIn(ConventionalItemTags.BLACK_DYES) -> DyeColor.BLACK
			else -> null
		}
	}

}