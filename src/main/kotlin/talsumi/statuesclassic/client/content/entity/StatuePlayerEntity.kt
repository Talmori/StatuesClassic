package talsumi.statuesclassic.client.content.entity

import com.mojang.authlib.GameProfile
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import talsumi.statuesclassic.client.core.SkinHandler
import talsumi.statuesclassic.content.blockentity.StatueBE

/**
 * A fake player entity that delegates to a [StatueBE] for some operations. Used by [StatuePlayerRenderer] for some operations that require an actual player.
 * This entity does not exist in the world!
 */
class StatuePlayerEntity(val statue: StatueBE, world: ClientWorld, pos: BlockPos, profile: GameProfile) : AbstractClientPlayerEntity(world, profile)
{
    init
    {
        this.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    override fun tick()
    {

    }

    override fun isSpectator(): Boolean = false

    override fun isCreative(): Boolean = false

    override fun getEquippedStack(slot: EquipmentSlot): ItemStack
    {
        return when (slot) {
            EquipmentSlot.MAINHAND -> statue.inventory.getStack(4)
            EquipmentSlot.OFFHAND -> statue.inventory.getStack(5)
            EquipmentSlot.FEET -> statue.inventory.getStack(3)
            EquipmentSlot.LEGS -> statue.inventory.getStack(2)
            EquipmentSlot.CHEST -> statue.inventory.getStack(1)
            EquipmentSlot.HEAD -> statue.inventory.getStack(0)
        }
    }

    override fun getActiveItem(): ItemStack = statue.inventory.getStack(4)

    override fun getMainHandStack(): ItemStack = statue.inventory.getStack(4)

    override fun getOffHandStack(): ItemStack = statue.inventory.getStack(5)
    override fun hasSkinTexture(): Boolean = true
    override fun getSkinTexture(): Identifier? = statue.playerUuid?.let { SkinHandler.getCachedSkin(it).getSkinOrDefault() }
    override fun canRenderCapeTexture(): Boolean = statue.hasCape

    override fun getCapeTexture(): Identifier? = statue.playerUuid?.let { SkinHandler.getCachedSkin(it).cape ?: null }

    override fun canRenderElytraTexture(): Boolean = canRenderCapeTexture()

    override fun getElytraTexture(): Identifier? = statue.playerUuid?.let { SkinHandler.getCachedSkin(it).elytra ?: null }

    override fun isPartVisible(modelPart: PlayerModelPart?): Boolean = true
}