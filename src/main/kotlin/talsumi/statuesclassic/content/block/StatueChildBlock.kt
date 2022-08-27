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
            world.removeBlock(pos, false)

        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult
    {
        val be = world.getBlockEntity(pos.down()) ?: return ActionResult.FAIL

        if (world.isClient) return ActionResult.SUCCESS

        if (be is StatueBE)
            if (!be.onRightClicked(player, hand, player.getStackInHand(hand)))
                player.openHandledScreen(StatueEquipmentScreenHandler.makeFactory(be))

        return ActionResult.SUCCESS
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