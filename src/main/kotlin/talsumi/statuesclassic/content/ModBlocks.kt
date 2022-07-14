package talsumi.statuesclassic.content

import net.minecraft.block.Block
import net.minecraft.block.Material
import talsumi.statuesclassic.content.block.StatueChildBlock
import talsumi.statuesclassic.content.block.StatueParentBlock
import talsumi.statuesclassic.marderlib.registration.EasyRegisterableHolder
import talsumi.statuesclassic.marderlib.util.RegUtil

object ModBlocks: EasyRegisterableHolder<Block>() {

    val statue_child = reg(StatueChildBlock(RegUtil.blockSettings(Material.STONE, hardness = 2f).nonOpaque().dropsNothing()))
    val statue_parent = reg(StatueParentBlock(RegUtil.blockSettings(Material.STONE, hardness = 2f).nonOpaque().dropsNothing()))
}