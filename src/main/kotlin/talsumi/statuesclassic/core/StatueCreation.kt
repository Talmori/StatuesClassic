package talsumi.statuesclassic.core

import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.block.AbstractStatueBlock
import talsumi.statuesclassic.content.blockentity.StatueBE
import java.util.*

object StatueCreation {

    fun tryCreateStatue(bottomPos: BlockPos, world: World, uuid: UUID, data: StatueData, facing: Direction)
    {
        if (StatuePlacement.canCreateStatueHere(world, bottomPos)) {
            val state = world.getBlockState(bottomPos)
            val block = world.getBlockState(bottomPos).block

            //Sanity check for facing
            if (facing == Direction.UP || facing == Direction.DOWN) return

            val light = state.luminance.coerceIn(0, 15)

            world.setBlockState(bottomPos, ModBlocks.statue_parent.defaultState.with(Properties.HORIZONTAL_FACING, facing).with(AbstractStatueBlock.lightLevel, light))
            world.setBlockState(bottomPos.up(), ModBlocks.statue_child.defaultState.with(AbstractStatueBlock.lightLevel, light))
            val be = world.getBlockEntity(bottomPos)

            if (be is StatueBE) {
                be.setup(block, uuid, data)
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
    }

    fun updateStatueHands(pos: BlockPos, world: World, left: Float, right: Float)
    {
        val be = world.getBlockEntity(pos)

        if (be is StatueBE) {
            //Joystick rotation is swapped. It's weird.
            be.leftHandRotate = right;
            be.rightHandRotate = left;
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
        data.masterRotate = joystick6X
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