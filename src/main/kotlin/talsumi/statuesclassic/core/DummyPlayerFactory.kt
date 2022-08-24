package talsumi.statuesclassic.core

import com.mojang.authlib.GameProfile
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.client.content.entity.DummyPlayerEntity

object DummyPlayerFactory {

    fun getDummyPlayer(statue: StatueBE, world: World, pos: BlockPos): PlayerEntity = DummyPlayerEntity(statue, world as ClientWorld, pos, GameProfile(null, "statuesclassic_fakeplayer"))
}