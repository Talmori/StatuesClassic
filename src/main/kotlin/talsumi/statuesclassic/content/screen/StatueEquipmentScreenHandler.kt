package talsumi.statuesclassic.content.screen

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import talsumi.marderlib.screenhandler.EnhancedScreenHandler
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueHelper

class StatueEquipmentScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, val inv: Inventory, val statue: StatueBE?) : EnhancedScreenHandler(type, syncId) {

    //Client Constructor
    constructor(syncId: Int, inv: PlayerInventory, buf: PacketByteBuf) : this(syncId, inv, SimpleInventory(6),
        MinecraftClient.getInstance()?.world?.getBlockEntity(buf.readBlockPos(), ModBlockEntities.statue)?.orElse(null))

    //Common Constructor
    constructor(syncId: Int, pInv: PlayerInventory, inv: Inventory, statue: StatueBE?) : this(ModScreenHandlers.statue_equipment_screen, syncId, inv, statue)
    {
        setup(pInv, inv)
    }

    fun setup(pInv: PlayerInventory, inv: Inventory)
    {
        addPlayerInventory(pInv, 8, 96)
        addSlotBox(inv, 0, 80, 8, 1, 4, 18, 18)
        addSlot(inv, 4, 61, 34)
        addSlot(inv, 5, 99, 34)
    }

    fun updateHands(left: Float, right: Float)
    {
        if (statue != null)
            StatueHelper.updateStatueHands(statue.pos, statue.world ?: return, left, right)
    }

    override fun canUse(player: PlayerEntity): Boolean = statue?.inventory?.canPlayerUse(player) == true && !statue?.isRemoved

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack = handleShiftClick(player, index, inv)

    companion object {
        fun makeFactory(statue: StatueBE): ExtendedScreenHandlerFactory
        {
            return object: ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler
                {
                    return StatueEquipmentScreenHandler(syncId, inv, statue.inventory, statue)
                }
                override fun getDisplayName(): Text {
                    return TranslatableText("")
                }
                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeBlockPos(statue.pos)
                }
            }
        }
    }
}