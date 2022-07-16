package talsumi.statuesclassic.content.block

import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE

class StatueParentBlock(settings: Settings) : AbstractStatueBlock(settings), BlockEntityProvider {

    init
    {
        defaultState = defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }
    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean)
    {
        if (world.getBlockState(pos.up()).block !is StatueChildBlock)
            world.removeBlock(pos, false)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult
    {
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean)
    {
        if (!state.isOf(newState.block)) {
            if (world.getBlockState(pos.up()).block is StatueChildBlock)
                world.removeBlock(pos.up(), false)
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>)
    {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = StatueBE(pos, state)
}