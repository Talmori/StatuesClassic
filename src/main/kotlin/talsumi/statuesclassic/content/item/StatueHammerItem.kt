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

package talsumi.statuesclassic.content.item

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import talsumi.statuesclassic.client.core.BlockColorLookups
import talsumi.statuesclassic.client.core.ModShaders
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.core.StatueHelper

class StatueHammerItem(settings: Settings) : Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext)
    {
        tooltip.add(TranslatableText("tooltip.statuesclassic.statue_hammer").formatted(Formatting.GRAY))
    }

    override fun useOnBlock(ctx: ItemUsageContext): ActionResult
    {
        val world = ctx.world
        if (world.isClient) return ActionResult.FAIL
        val pos = ctx.blockPos
        val state = world.getBlockState(pos)
        val player = ctx.player ?: return ActionResult.FAIL

        println(BlockColorLookups.getBlockColour(state))

        if (!StatueHelper.isBlockValidForStatue(world, pos))
            return ActionResult.FAIL

        val down = pos.down()
        val up = pos.up()

        //Check up for second block
        if (world.getBlockState(up) == state && StatueHelper.isBlockValidForStatue(world, up))
            player.openHandledScreen(StatueCreationScreenHandler.makeFactory(player, ctx.side, pos, world))
        //Check down for second block
        else if (world.getBlockState(down) == state && StatueHelper.isBlockValidForStatue(world, down))
            player.openHandledScreen(StatueCreationScreenHandler.makeFactory(player, ctx.side, down, world))

        return ActionResult.SUCCESS
    }

    //This was used when I was developing the statue shaders. It's very useful!
    /*
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack>
    {
        if (user.isSneaking && world.isClient) {
            ModShaders.reload()
            user.sendMessage(Text.of("Reloaded mod shaders!"), false)
        }

        return super.use(world, user, hand)
    }
    */
}