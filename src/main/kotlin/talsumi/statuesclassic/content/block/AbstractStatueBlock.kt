package talsumi.statuesclassic.content.block

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Waterloggable
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import talsumi.statuesclassic.core.StatueHelper

abstract class AbstractStatueBlock(settings: Settings) : Block(settings), Waterloggable {

    companion object {
        val lightLevel = IntProperty.of("light_level", 0, 15)

        fun luminance(state: BlockState): Int = state.get(lightLevel)
    }

    init
    {
        defaultState = defaultState.with(lightLevel, 0).with(Properties.WATERLOGGED, false)
    }

    fun replace(world: World, pos1: BlockPos, pos2: BlockPos)
    {
        val replacement = StatueHelper.removeStatue(world, pos1)
        val pos2Valid = world.getBlockState(pos2).block is AbstractStatueBlock
        if (pos2Valid) {
            world.removeBlock(pos2, false)
            world.setBlockState(pos2, replacement)
        }
        world.setBlockState(pos1, replacement)
    }

    override fun getPistonBehavior(state: BlockState?): PistonBehavior = PistonBehavior.BLOCK

    override fun getFluidState(state: BlockState): FluidState = if (state.get(Properties.WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)

    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState
    {
        if (state.get(Properties.WATERLOGGED))
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun isTranslucent(state: BlockState?, world: BlockView?, pos: BlockPos?): Boolean = true

    override fun getRenderType(state: BlockState?): BlockRenderType? = BlockRenderType.INVISIBLE

    override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float = 1f

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>)
    {
        builder.add(lightLevel)
        builder.add(Properties.WATERLOGGED)
    }
}