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

import net.minecraft.block.Block
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

object ShapeUtil {

	fun makePipeShapes(radius: Int): Array<VoxelShape>
	{
		return arrayOf(
			makePipeShape(Direction.DOWN, radius),
			makePipeShape(Direction.UP, radius),
			makePipeShape(Direction.NORTH, radius),
			makePipeShape(Direction.SOUTH, radius),
			makePipeShape(Direction.EAST, radius),
			makePipeShape(Direction.WEST, radius))
	}

	/**
	 * Convenience method to create hitboxes for pipes.
	 */
	fun makePipeShape(dir: Direction?, radius: Int): VoxelShape
	{
		val min = 8.0-radius
		val max = 8.0+radius

		return when (dir) {
			Direction.NORTH -> Block.createCuboidShape(min, min, 0.0, max, max, min)
			Direction.SOUTH -> Block.createCuboidShape(min, min, max, max, max, 16.0)
			Direction.EAST -> Block.createCuboidShape(0.0, min, min, min, max, max)
			Direction.WEST -> Block.createCuboidShape(max, min, min, 16.0, max, max)
			Direction.UP -> Block.createCuboidShape(min, max, min, max, 16.0, max)
			Direction.DOWN -> Block.createCuboidShape(min, 0.0, min, max, min, max)
			null -> Block.createCuboidShape(min, min, min, max, max, max)
		}
	}
}