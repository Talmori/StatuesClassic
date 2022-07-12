package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

abstract class AbstractStatueBlock(settings: Settings) : Block(settings) {
    override fun isTranslucent(state: BlockState?, world: BlockView?, pos: BlockPos?): Boolean = true

    override fun getRenderType(state: BlockState?): BlockRenderType? = BlockRenderType.INVISIBLE

    override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float = 1f
}