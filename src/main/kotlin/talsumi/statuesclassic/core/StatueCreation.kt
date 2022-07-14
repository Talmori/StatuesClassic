package talsumi.statuesclassic.core

import com.mojang.authlib.Agent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.blockentity.StatueBE

object StatueCreation {

    fun tryCreateStatue(bottomPos: BlockPos, world: World, data: StatueData)
    {
        if (StatuePlacement.canCreateStatueHere(world, bottomPos)) {
            world.setBlockState(bottomPos, ModBlocks.statue_parent.defaultState)
            world.setBlockState(bottomPos.up(), ModBlocks.statue_child.defaultState)
            val be = world.getBlockEntity(bottomPos) as StatueBE
            val statueId = be.statueId

            println(world.time)
            UuidLookup.lookupUuidFromServer(world.server ?: return, data.player) {
                val be = world.getBlockEntity(bottomPos)
                if (be is StatueBE && be.statueId == statueId)
                    be.setup(it, data)
                println(world.time)
            }
        }
    }
}