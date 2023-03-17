package talsumi.statuesclassic
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.ModItems
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.networking.ServerPacketHandlers

//TODO: Compat with ears. Requires StatuePlayerRenderer to retrieve skin from StatuePlayerEntity & keeping 'trigger' area in mixed skins.
@Suppress("UNUSED")
object StatuesClassic: ModInitializer {

    const val MODID = "statuesclassic"

    val LOGGER: Logger = LogManager.getLogger()
    val GROUP: ItemGroup = FabricItemGroup.builder(Identifier(MODID, MODID)).icon { ItemStack(Items.PLAYER_HEAD) }.build()

    override fun onInitialize() {
        val sTime = System.currentTimeMillis()
        LOGGER.info("Statues Classic initializing...")
        val sRegTime = System.currentTimeMillis()
        ModBlocks.regAll(Registries.BLOCK, Block::class, MODID)
        ModItems.regAll(Registries.ITEM, Item::class, MODID)

        ModBlockEntities.regAll(Registries.BLOCK_ENTITY_TYPE, BlockEntityType::class, MODID)

        ItemGroupEvents.modifyEntriesEvent(GROUP).register {
            it.add(ModItems.statue_hammer)
            it.add(ModItems.palette)
        }

        val eRegTime = System.currentTimeMillis()

        ModScreenHandlers.wake()
        ServerPacketHandlers.register()

        val eTime = System.currentTimeMillis()
        LOGGER.info("Statues Classic initialization complete in ${eTime-sTime} milliseconds.")
    }
}