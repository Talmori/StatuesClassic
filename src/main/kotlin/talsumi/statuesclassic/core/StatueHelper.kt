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

package talsumi.statuesclassic.core

import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import net.minecraft.world.World
import talsumi.marderlib.util.VectorUtil
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.block.AbstractStatueBlock
import talsumi.statuesclassic.content.blockentity.StatueBE
import java.util.*

object StatueHelper {

    fun isBlockValidForStatue(world: World, pos: BlockPos): Boolean
    {
        val state = world.getBlockState(pos)
        if (state.hasBlockEntity() || world.getBlockEntity(pos) != null || state.isAir || !state.isFullCube(world, pos) || state.block.hardness < 0)
            return false

        return true
    }

    fun canCreateStatueHere(world: World, pos: BlockPos): Boolean
    {
        val state = world.getBlockState(pos)
        val stateUp = world.getBlockState(pos.up())

        return isBlockValidForStatue(world, pos) && isBlockValidForStatue(world, pos.up()) && state == stateUp
    }

    fun tryCreateStatue(bottomPos: BlockPos, world: World, name: String, uuid: UUID, data: StatueData, facing: Direction)
    {
        if (canCreateStatueHere(world, bottomPos)) {
            val state = world.getBlockState(bottomPos)
            val block = world.getBlockState(bottomPos)

            //Sanity check for facing
            if (facing == Direction.UP || facing == Direction.DOWN) return

            val light = state.luminance.coerceIn(0, 15)

            world.setBlockState(bottomPos, ModBlocks.statue_parent.defaultState.with(Properties.HORIZONTAL_FACING, facing).with(AbstractStatueBlock.lightLevel, light))
            world.setBlockState(bottomPos.up(), ModBlocks.statue_child.defaultState.with(AbstractStatueBlock.lightLevel, light))
            val be = world.getBlockEntity(bottomPos)

            if (be is StatueBE) {
                be.setup(block, name, uuid, data)
                world.server?.execute { be.sendUpdatePacket() }
            }
        }
    }

    fun modifyStatueLuminance(be: StatueBE, luminance: Int)
    {
        val world = be.world ?: return
        val childPos = be.pos.up()
        val block = world.getBlockState(be.pos)
        val childBlock = world.getBlockState(childPos)

        if (block.block !is AbstractStatueBlock || childBlock.block !is AbstractStatueBlock) return

        world.setBlockState(be.pos, block.with(AbstractStatueBlock.lightLevel, luminance))
        world.setBlockState(childPos, childBlock.with(AbstractStatueBlock.lightLevel, luminance))
        be.markDirty()
    }

    fun removeStatue(block: AbstractStatueBlock)
    {

    }

    fun removeStatue(be: StatueBE)
    {
        val world = be.world
        val state = be.block
    }

    fun getStatueLuminance(be: StatueBE): Int = be.cachedState.get(AbstractStatueBlock.lightLevel)

    fun updateStatueHands(pos: BlockPos, world: World, left: Float, right: Float)
    {
        val be = world.getBlockEntity(pos)

        if (be is StatueBE) {
            //Joystick rotation is swapped. It's weird.
            be.leftHandRotate = right;
            be.rightHandRotate = left;
            be.markDirty()
            be.sendUpdatePacket()
        }
    }

    fun applyJoystickAnglesToStatueData(data: StatueData,
                                        joystick1Y: Float,
                                        joystick1X: Float,
                                        joystick2Y: Float,
                                        joystick2X: Float,
                                        joystick3Y: Float,
                                        joystick3X: Float,
                                        joystick4Y: Float,
                                        joystick4X: Float,
                                        joystick5Y: Float,
                                        joystick5X: Float,
                                        joystick6Y: Float,
                                        joystick6X: Float)
    {
        data.leftArmRaise = (joystick1Y * 180f) * MathHelper.RADIANS_PER_DEGREE
        data.leftArmRotate = -joystick1X
        data.rightArmRaise = (joystick2Y * 180f) * MathHelper.RADIANS_PER_DEGREE
        data.rightArmRotate = -joystick2X
        data.leftLegRaise = (joystick3Y * 100f) * MathHelper.RADIANS_PER_DEGREE
        data.leftLegRotate = -joystick3X
        data.rightLegRaise = (joystick4Y * 100f) * MathHelper.RADIANS_PER_DEGREE
        data.rightLegRotate = -joystick4X
        data.headRaise = (joystick5Y * 75f) * MathHelper.RADIANS_PER_DEGREE
        data.headRotate = (-joystick5X * 90f) * MathHelper.RADIANS_PER_DEGREE
        data.masterRaise = joystick6Y
        data.masterRotate = -joystick6X

        val rightLegHeightOffset = Vec3f(0f, 0.5f, 0f)
        rightLegHeightOffset.add(VectorUtil.multiply(Vec3f(0f, data.rightLegRaise, 0f), 1f, 0.75f, 1f))
        val leftLegHeightOffset = Vec3f(0f, 0.5f, 0f)
        leftLegHeightOffset.add(VectorUtil.multiply(Vec3f(0f, data.leftLegRaise, 0f), 1f, 0.75f, 1f))

        data.heightOffset = -(rightLegHeightOffset.y.coerceAtLeast(leftLegHeightOffset.y) + 0.05f).coerceAtMost(0f)
    }

    /**
     * Turns joystick position into rotation in degrees.
     */
    fun encodeHandRotation(joystick: Float): Float = (joystick * 90f) * MathHelper.RADIANS_PER_DEGREE

    /**
     * Turns rotation into joystick position
     */
    fun decodeHandRotation(rotation: Float): Float = (rotation / 90) * MathHelper.DEGREES_PER_RADIAN
}