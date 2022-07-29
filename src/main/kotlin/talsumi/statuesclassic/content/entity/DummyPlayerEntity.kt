package talsumi.statuesclassic.content.entity

import com.mojang.authlib.GameProfile
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import talsumi.statuesclassic.content.blockentity.StatueBE

class DummyPlayerEntity(val statue: StatueBE, world: World, pos: BlockPos, yaw: Float, profile: GameProfile) : PlayerEntity(world, pos, yaw, profile)
{
    override fun isSpectator(): Boolean = false

    override fun isCreative(): Boolean = false
}