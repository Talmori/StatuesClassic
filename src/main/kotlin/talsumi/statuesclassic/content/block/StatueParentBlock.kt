package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler

class StatueParentBlock(settings: Settings) : AbstractStatueBlock(settings), BlockEntityProvider {

    init
    {
        defaultState = defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean)
    {
        if (world.getBlockState(pos.up()).block !is StatueChildBlock)
            replace(world, pos.up(), pos)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult
    {
        val be = world.getBlockEntity(pos) ?: return ActionResult.FAIL

        if (world.isClient) return ActionResult.SUCCESS

        if (be is StatueBE)
            if (!be.onRightClicked(player, hand, player.getStackInHand(hand), player.isSneaking))
                player.openHandledScreen(StatueEquipmentScreenHandler.makeFactory(be))

        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean)
    {
        if (!state.isOf(newState.block)) {
            if (world.getBlockState(pos.up()).block is StatueChildBlock)
                replace(world, pos, pos.up())
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>)
    {
        super.appendProperties(builder)
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = StatueBE(pos, state)

    //Not needed currently.
    /*override fun <T : BlockEntity?> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>?
    {
        //We only need to tick clientside.
        return if (world.isClient) BlockEntityTicker { world, pos, state, be ->  (be as? StatueBE)?.tick() } else null
    }*/
}