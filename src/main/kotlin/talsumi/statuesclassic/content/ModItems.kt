package talsumi.statuesclassic.content

import net.minecraft.item.Item
import talsumi.marderlib.registration.EasyRegisterableHolder
import talsumi.marderlib.util.RegUtil
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.item.StatueHammerItem

object ModItems: EasyRegisterableHolder<Item>() {

    val statue_hammer = reg(StatueHammerItem(RegUtil.itemSettings(StatuesClassic.GROUP, maxCount = 1)))
}