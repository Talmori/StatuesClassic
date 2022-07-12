package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class StatueChildBlock(settings: Settings) : AbstractStatueBlock(settings) {

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean)
    {
        if (world.getBlockState(pos.down()).block !is StatueParentBlock)
            world.removeBlock(pos, false)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean)
    {
        if (!state.isOf(newState.block)) {
            if (world.getBlockState(pos.down()).block is StatueParentBlock)
                world.removeBlock(pos.down(), false)
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }
}