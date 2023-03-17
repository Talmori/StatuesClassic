package talsumi.statuesclassic.core

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
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

    fun removeStatue(world: World, pos: BlockPos): BlockState
    {
        val be = (world.getBlockEntity(pos) ?: world.getBlockEntity(pos.down())) as? StatueBE ?: return Blocks.STONE.defaultState
        return removeStatue(be)
    }

    fun removeStatue(be: StatueBE): BlockState
    {
        be.statueRemoved()
        return be.block ?: Blocks.STONE.defaultState
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

        val rightLegHeightOffset = Vec3d(0.0, 0.5, 0.0).add(Vec3d(1.0, 0.75, 1.0).multiply(0.0, data.rightLegRaise.toDouble(), 0.0))
        val leftLegHeightOffset = Vec3d(0.0, 0.5, 0.0).add(Vec3d(1.0, 0.75, 1.0).multiply(0.0, data.leftLegRaise.toDouble(), 0.0))

        data.heightOffset = -(rightLegHeightOffset.y.coerceAtLeast(leftLegHeightOffset.y) + 0.05f).coerceAtMost(0.0).toFloat()
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