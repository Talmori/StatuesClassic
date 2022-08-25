package talsumi.statuesclassic.core

import com.mojang.authlib.GameProfile
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.client.content.entity.DummyPlayerEntity

object DummyPlayerFactory {

    fun getDummyPlayer(statue: StatueBE, world: World, pos: BlockPos): PlayerEntity
    {
        val ent = DummyPlayerEntity(statue, world as ClientWorld, pos, GameProfile(null, "statuesclassic_fakeplayer"))
        ent.setPos(pos.x + 0.5, pos.y + 0.0, pos.z + 0.5)
        return ent
    }
}