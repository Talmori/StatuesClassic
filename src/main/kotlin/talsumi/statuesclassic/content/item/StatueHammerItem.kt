package talsumi.statuesclassic.content.item

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class StatueHammerItem(settings: Settings) : Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext)
    {
        tooltip.add(TranslatableText("tooltip.statuesclassic.statue_hammer").formatted(Formatting.GRAY))
    }
}