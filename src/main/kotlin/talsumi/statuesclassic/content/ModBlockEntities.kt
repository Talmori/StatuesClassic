package talsumi.statuesclassic.content

import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import talsumi.marderlib.registration.EasyRegisterableHolder
import talsumi.statuesclassic.content.blockentity.StatueBE

object ModBlockEntities: EasyRegisterableHolder<BlockEntityType<*>>() {

    val statue = reg(FabricBlockEntityTypeBuilder.create(::StatueBE, ModBlocks.statue_parent).build(null))
}