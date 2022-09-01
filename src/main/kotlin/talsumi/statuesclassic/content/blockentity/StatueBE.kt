/*
 * MIT License
 *
 * Copyright (c) 2022 Talsumi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package talsumi.statuesclassic.content.blockentity

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import talsumi.marderlib.content.IUpdatableBlockEntity
import talsumi.marderlib.storage.SlotLimitations
import talsumi.marderlib.storage.item.ItemStackHandler
import talsumi.marderlib.util.ItemStackUtil
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModItems
import talsumi.statuesclassic.core.DummyPlayerFactory
import talsumi.statuesclassic.core.StatueHelper
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.networking.ServerPacketsOut
import java.util.*

class StatueBE(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.statue, pos, state), IUpdatableBlockEntity {

    /**
     * 0: Head
     * 1: Chest
     * 2: Legs
     * 3: Boots
     * 4: Right hand
     * 5: Left hand
     */
    val inventory = ItemStackHandler(6, ::onContentsChanged, arrayOf(
        SlotLimitations(0, 0, allowed = { item -> item.item is BlockItem || (item.item as? ArmorItem)?.slotType == EquipmentSlot.HEAD || item.item is Wearable}),
        SlotLimitations(1, 1, allowed = { item -> (item.item as? ArmorItem)?.slotType == EquipmentSlot.CHEST || item.item is Wearable }),
        SlotLimitations(2, 2, allowed = { item -> (item.item as? ArmorItem)?.slotType == EquipmentSlot.LEGS || item.item is Wearable }),
        SlotLimitations(3, 3, allowed = { item -> (item.item as? ArmorItem)?.slotType == EquipmentSlot.FEET || item.item is Wearable })
    ))

    var hasBeenSetup = false
    var playerUuid: UUID? = null
    var playerName: String? = null
    var data: StatueData? = null
    var block: Block? = null

    var hasCape = false
    var isColoured = false
    var hasName = false
    var leftHandRotate = 0f
    var rightHandRotate = 0f

    var statueId = UUID.randomUUID()

    /**
     * Note: This is an AbstractClientPlayerEntity on client.
     * The entity is not present in the world, and *should* be GC'ed when this BlockEntity is.
     */
    var clientFakePlayer: PlayerEntity? = null

    //Currently disabled. (See StatueParentBlock to re-enable if needed)
    //Only called on client.
    /*
    fun tick()
    {
        clientFakePlayer?.tick()
    }*/

    //TODO: This could do with a cleanup
    /**
     * Called when either of the statue's blocks is activated with an item.
     * Returns true if further processing is to be skipped.
     */
    fun onRightClicked(player: PlayerEntity, hand: Hand, item: ItemStack, sneaking: Boolean): Boolean
    {
        val override = when (item.item) {
            Items.LEATHER -> { hasCape = !hasCape; if (hasCape) item.decrement(1) else ItemStackUtil.dropStack(world!!, player.pos, Items.LEATHER.defaultStack); true }
            Items.PAPER -> { hasName = !hasName; if (hasName) item.decrement(1) else ItemStackUtil.dropStack(world!!, player.pos, Items.PAPER.defaultStack); true }
            ModItems.palette -> { if (!isColoured) item.decrement(1); isColoured = true; true }
            Items.GUNPOWDER -> { if (StatueHelper.getStatueLuminance(this) > 0) { StatueHelper.modifyStatueLuminance(this, 0); item.decrement(1);}; true }
            Items.GLOWSTONE_DUST -> { if (StatueHelper.getStatueLuminance(this) < 15) { StatueHelper.modifyStatueLuminance(this, 15); item.decrement(1);}; true }
            else -> false
        }

        if (override) {
            sendUpdatePacket()
            markDirty()
        }

        return override
    }

    fun setup(block: Block, name: String, uuid: UUID, data: StatueData)
    {
        this.playerUuid = uuid
        this.data = data
        this.playerName = name
        this.block = block
        hasBeenSetup = true
        markDirty()
        StatuesClassic.LOGGER.info("Created statue at $pos, for player $uuid, made of $block")
    }

    override fun setWorld(world: World)
    {
        super.setWorld(world)

        if (world.isClient)
            clientFakePlayer = DummyPlayerFactory.getDummyPlayer(this, world, pos)
    }

    fun sendUpdatePacket()
    {
        for (player in PlayerLookup.tracking(this))
            talsumi.marderlib.networking.ServerPacketsOut.sendUpdateBlockEntityPacket(this, player)
    }

    private fun onContentsChanged()
    {
        sendUpdatePacket()
        markDirty()
    }

    override fun writeUpdatePacket(buf: PacketByteBuf)
    {
        buf.writeBoolean(hasBeenSetup)
        if (hasBeenSetup) {
            inventory.saveToByteBuf(buf)
            buf.writeUuid(playerUuid)
            buf.writeString(playerName)
            buf.writeBoolean(data != null)
            data?.writePacket(buf)
            buf.writeString(block!!.registryEntry.registryKey().value.toString())
            buf.writeFloat(leftHandRotate)
            buf.writeFloat(rightHandRotate)
            buf.writeBoolean(hasCape)
            buf.writeBoolean(isColoured)
            buf.writeBoolean(hasName)
        }
    }

    override fun receiveUpdatePacket(buf: PacketByteBuf)
    {
        hasBeenSetup = buf.readBoolean()
        if (hasBeenSetup) {
            inventory.loadFromByteBuf(buf)
            playerUuid = buf.readUuid()
            playerName = buf.readString()
            if (buf.readBoolean())
                data = StatueData.fromPacket(buf)
            block = Registry.BLOCK.get(Identifier(buf.readString()))
            leftHandRotate = buf.readFloat()
            rightHandRotate = buf.readFloat()
            hasCape = buf.readBoolean()
            isColoured = buf.readBoolean()
            hasName = buf.readBoolean()
        }
    }

    override fun readNbt(nbt: NbtCompound)
    {
        inventory.load(nbt.getCompound("inventory"))
        hasBeenSetup = nbt.getBoolean("has_setup")
        if (hasBeenSetup) {
            data = StatueData.load(nbt.getCompound("statue_data"))
            playerUuid = nbt.getUuid("player_uuid")
            playerName = nbt.getString("player_name")
            block = Registry.BLOCK.get(Identifier(nbt.getString("block")))
            leftHandRotate = nbt.getFloat("left_hand_rotate")
            rightHandRotate = nbt.getFloat("right_hand_rotate")
            hasCape = nbt.getBoolean("has_cape")
            isColoured = nbt.getBoolean("is_coloured")
            hasName = nbt.getBoolean("shows_name")
        }
    }

    override fun writeNbt(nbt: NbtCompound)
    {
        nbt.put("inventory", inventory.save())
        nbt.putBoolean("has_setup", hasBeenSetup)
        if (hasBeenSetup) {
            nbt.put("statue_data", data!!.save())
            nbt.putUuid("player_uuid", playerUuid!!)
            nbt.putString("player_name", playerName!!)
            nbt.putString("block", block!!.registryEntry.registryKey().value.toString())
            nbt.putFloat("left_hand_rotate", leftHandRotate)
            nbt.putFloat("right_hand_rotate", rightHandRotate)
            nbt.putBoolean("has_cape", hasCape)
            nbt.putBoolean("is_coloured", isColoured)
            nbt.putBoolean("shows_name", hasName)
        }
    }
}