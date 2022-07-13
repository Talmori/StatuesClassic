package talsumi.statuesclassic.core

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object StatuePlacement {

    fun isBlockValidForStatue(world: World, pos: BlockPos): Boolean
    {
        val state = world.getBlockState(pos)
        if (state.hasBlockEntity() || world.getBlockEntity(pos) != null)
            return false
        if (state.isAir || !state.isFullCube(world, pos))
            return false

        return true
    }
}