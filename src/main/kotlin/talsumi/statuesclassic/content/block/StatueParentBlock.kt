package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE

class StatueParentBlock(settings: Settings) : AbstractStatueBlock(settings), BlockEntityProvider {

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean)
    {
        if (world.getBlockState(pos.up()).block !is StatueChildBlock)
            world.removeBlock(pos, false)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean)
    {
        if (!state.isOf(newState.block)) {
            if (world.getBlockState(pos.up()).block is StatueChildBlock)
                world.removeBlock(pos.up(), false)
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = StatueBE(pos, state)
}