package talsumi.statuesclassic.content.item

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.world.World
import talsumi.marderlib.compat.MLCompatText
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.core.StatueHelper

class StatueHammerItem(settings: Settings) : Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext)
    {
        tooltip.add(MLCompatText.makeTranslatableText("tooltip.statuesclassic.statue_hammer").formatted(Formatting.GRAY))
    }

    override fun useOnBlock(ctx: ItemUsageContext): ActionResult
    {
        val world = ctx.world
        if (world.isClient) return ActionResult.FAIL
        val pos = ctx.blockPos
        val state = world.getBlockState(pos)
        val player = ctx.player ?: return ActionResult.FAIL

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
}