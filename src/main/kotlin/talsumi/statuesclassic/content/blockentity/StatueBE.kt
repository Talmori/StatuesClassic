package talsumi.statuesclassic.content.blockentity

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import talsumi.marderlib.content.IUpdatableBlockEntity
import talsumi.marderlib.storage.SlotLimitations
import talsumi.marderlib.storage.item.ItemStackHandler
import talsumi.marderlib.util.BlockStateUtil
import talsumi.marderlib.util.ItemStackUtil
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModItems
import talsumi.statuesclassic.core.StatuePlayerEntityFactory
import talsumi.statuesclassic.core.StatueHelper
import talsumi.statuesclassic.core.StatueData
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
        SlotLimitations(0, 0, allowed = { item -> item.item is BlockItem || LivingEntity.getPreferredEquipmentSlot(item.toStack()) == EquipmentSlot.HEAD}),
        SlotLimitations(1, 1, allowed = { item -> LivingEntity.getPreferredEquipmentSlot(item.toStack()) == EquipmentSlot.CHEST }),
        SlotLimitations(2, 2, allowed = { item -> LivingEntity.getPreferredEquipmentSlot(item.toStack()) == EquipmentSlot.LEGS }),
        SlotLimitations(3, 3, allowed = { item -> LivingEntity.getPreferredEquipmentSlot(item.toStack()) == EquipmentSlot.FEET })
    ))

    var hasBeenSetup = false
    var playerUuid: UUID? = null
    var playerName: String? = null
    var data: StatueData? = null
    var block: BlockState? = null

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
        if (!hasBeenSetup)
            return false

        val override = when (item.item) {
            Items.LEATHER -> { hasCape = !hasCape; if (hasCape) item.decrement(1) else ItemStackUtil.dropStack(world!!, player.pos, Items.LEATHER.defaultStack); true }
            Items.PAPER -> { hasName = !hasName; if (hasName) item.decrement(1) else ItemStackUtil.dropStack(world!!, player.pos, Items.PAPER.defaultStack); true }
            ModItems.palette -> { isColoured = !isColoured; if (isColoured) item.decrement(1) else ItemStackUtil.dropStack(world!!, player.pos, ModItems.palette.defaultStack); true }
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

    fun statueRemoved()
    {
        for (item in inventory.getItems())
            ItemStackUtil.dropStack(world!!, Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), item, 10)
        if (hasCape)
            ItemStackUtil.dropStack(world!!, Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), Items.LEATHER.defaultStack, 10)
        if (hasName)
            ItemStackUtil.dropStack(world!!, Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), Items.PAPER.defaultStack, 10)
        if (isColoured)
            ItemStackUtil.dropStack(world!!, Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), ModItems.palette.defaultStack, 10)
    }

    fun setup(block: BlockState, name: String, uuid: UUID, data: StatueData)
    {
        this.playerUuid = uuid
        this.data = data
        this.playerName = name
        this.block = block
        hasBeenSetup = true
        markDirty()
        StatuesClassic.LOGGER.debug("Created statue at $pos, for player $uuid, made of $block")
    }

    override fun setWorld(world: World)
    {
        super.setWorld(world)

        if (world.isClient)
            clientFakePlayer = StatuePlayerEntityFactory.getStatuePlayer(this, world, pos)
    }

    fun sendUpdatePacket()
    {
        for (player in PlayerLookup.tracking(this))
            talsumi.marderlib.networking.ServerPacketsOut.sendUpdateBlockEntityPacket(this, player)
    }

    private fun onContentsChanged()
    {
        if (hasBeenSetup)
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
            buf.writeString(block!!.toString())
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
            block = BlockStateUtil.blockStateFromString(buf.readString())
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
            block = BlockStateUtil.blockStateFromString(nbt.getString("block"))
            leftHandRotate = nbt.getFloat("left_hand_rotate")
            rightHandRotate = nbt.getFloat("right_hand_rotate")
            hasCape = nbt.getBoolean("has_cape")
            isColoured = nbt.getBoolean("is_coloured")
            hasName = nbt.getBoolean("shows_name")

            //If we are made of an invalid block, mark the statue as un-setup.
            if (block == null) {
                hasBeenSetup = false
                markDirty()
            }
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
            nbt.putString("block", block!!.toString())
            nbt.putFloat("left_hand_rotate", leftHandRotate)
            nbt.putFloat("right_hand_rotate", rightHandRotate)
            nbt.putBoolean("has_cape", hasCape)
            nbt.putBoolean("is_coloured", isColoured)
            nbt.putBoolean("shows_name", hasName)
        }
    }
}