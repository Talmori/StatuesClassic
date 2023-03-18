package talsumi.statuesclassic.content

import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import talsumi.marderlib.registration.EasyRegisterableHolder
import talsumi.marderlib.util.RegUtil
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.item.StatueHammerItem

object ModItems: EasyRegisterableHolder<Item>() {

    val statue_hammer = reg(StatueHammerItem(RegUtil.itemSettings(maxCount = 1)))
    val palette = reg(Item(RegUtil.itemSettings()))
}