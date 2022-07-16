package talsumi.statuesclassic.core

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.blockentity.StatueBE
import java.util.*

object StatueCreation {

    fun tryCreateStatue(bottomPos: BlockPos, world: World, uuid: UUID, data: StatueData, facing: Direction)
    {
        if (StatuePlacement.canCreateStatueHere(world, bottomPos)) {
            val block = world.getBlockState(bottomPos).block
            world.setBlockState(bottomPos, ModBlocks.statue_parent.defaultState.with(Properties.HORIZONTAL_FACING, facing))
            world.setBlockState(bottomPos.up(), ModBlocks.statue_child.defaultState)
            val be = world.getBlockEntity(bottomPos)

            if (be is StatueBE) {
                be.setup(block, uuid, data)
                world.server?.execute { be.sendUpdatePacket() }
            }
        }
    }
}