package talsumi.statuesclassic.content

import net.minecraft.item.Item
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.item.StatueHammerItem
import talsumi.statuesclassic.marderlib.registration.EasyRegisterableHolder
import talsumi.statuesclassic.marderlib.util.RegUtil

object ModItems: EasyRegisterableHolder<Item>() {

    val statue_hammer = reg(StatueHammerItem(RegUtil.itemSettings(StatuesClassic.GROUP, maxCount = 1)))
}