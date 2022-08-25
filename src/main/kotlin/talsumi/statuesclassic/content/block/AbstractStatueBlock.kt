package talsumi.statuesclassic.content.block

import io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Waterloggable
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundEvent
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import java.util.*

abstract class AbstractStatueBlock(settings: Settings) : Block(settings), Waterloggable {

    companion object {
        val lightLevel = IntProperty.of("light_level", 0, 15)
    }

    init
    {
        defaultState = defaultState.with(lightLevel, 0).with(Properties.WATERLOGGED, false)
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