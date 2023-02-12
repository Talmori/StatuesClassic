package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler

class StatueChildBlock(settings: Settings) : AbstractStatueBlock(settings) {

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean)
    {
        if (world.getBlockState(pos.down()).block !is StatueParentBlock)
            replace(world, pos.down(), pos)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult
    {
        val be = world.getBlockEntity(pos.down()) ?: return ActionResult.FAIL

        if (world.isClient) return ActionResult.SUCCESS

        if (be is StatueBE)
            if (!be.onRightClicked(player, hand, player.getStackInHand(hand), player.isSneaking))
                player.openHandledScreen(StatueEquipmentScreenHandler.makeFactory(be))

        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean)
    {
        if (!state.isOf(newState.block)) {
            if (world.getBlockState(pos.down()).block is StatueParentBlock) {
                replace(world, pos, pos.down())
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }
}