package talsumi.statuesclassic.content.screen

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import talsumi.marderlib.screenhandler.EnhancedScreenHandler
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.core.StatueHelper
import java.util.*

class StatueCreationScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, val hitFace: Direction?, val playerFacing: Direction?, val parentPos: BlockPos, val world: World?) : EnhancedScreenHandler(type, syncId) {

    //Client Constructor
    constructor(syncId: Int, inv: PlayerInventory, buf: PacketByteBuf) : this(syncId, null, null, buf.readBlockPos(), null)

    //Common Constructor
    constructor(syncId: Int, hitFace: Direction?, playerFacing: Direction?, parentPos: BlockPos, world: World?) : this(ModScreenHandlers.statue_creation_screen, syncId, hitFace, playerFacing, parentPos, world) {
        setup()
    }

    fun setup() {

    }

    fun form(name: String, uuid: UUID, data: StatueData)
    {
        if (world != null) {
            val direction = if (hitFace == Direction.UP || hitFace == Direction.DOWN) playerFacing!!.opposite else hitFace!!.opposite
            StatueHelper.tryCreateStatue(parentPos!!, world!!, name, uuid, data, direction)
        }
        for (listener in getListeners())
            listener.marderlib_getOwningPlayer().closeHandledScreen()
    }

    override fun canUse(player: PlayerEntity): Boolean
    {
        return true
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack = ItemStack.EMPTY

    companion object {
        fun makeFactory(player: PlayerEntity, hitFace: Direction, pos: BlockPos, world: World): ExtendedScreenHandlerFactory
        {
            return object: ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler?
                {
                    return StatueCreationScreenHandler(syncId, player.horizontalFacing, hitFace, pos, world)
                }
                override fun getDisplayName(): Text {
                    return Text.of("")
                }
                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeBlockPos(pos)
                }
            }
        }
    }
}