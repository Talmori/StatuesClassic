package talsumi.statuesclassic.core

import com.mojang.authlib.GameProfile
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.client.content.entity.StatuePlayerEntity
import talsumi.statuesclassic.content.blockentity.StatueBE

object StatuePlayerEntityFactory {

    fun getStatuePlayer(statue: StatueBE, world: World, pos: BlockPos): PlayerEntity
    {
        val ent = StatuePlayerEntity(statue, world as ClientWorld, pos, GameProfile(null, "statuesclassic_fakeplayer"))
        ent.setPos(pos.x + 0.5, pos.y + 0.0, pos.z + 0.5)
        return ent
    }
}