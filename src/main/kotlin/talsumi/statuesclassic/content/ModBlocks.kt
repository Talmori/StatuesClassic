package talsumi.statuesclassic.content

import net.minecraft.block.Block
import net.minecraft.block.Material
import talsumi.marderlib.registration.EasyRegisterableHolder
import talsumi.marderlib.util.RegUtil
import talsumi.statuesclassic.content.block.StatueChildBlock
import talsumi.statuesclassic.content.block.StatueParentBlock

object ModBlocks: EasyRegisterableHolder<Block>() {

    val statue_child = reg(StatueChildBlock(RegUtil.blockSettings(Material.STONE, hardness = 2f).nonOpaque().dropsNothing()))
    val statue_parent = reg(StatueParentBlock(RegUtil.blockSettings(Material.STONE, hardness = 2f).nonOpaque().dropsNothing()))
}