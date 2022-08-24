/*
 * MIT License
 *
 *  Copyright (c) 2022 Talsumi
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *
 */

package talsumi.statuesclassic
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.content.ModItems
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.networking.ServerPacketHandlers

//TODO: Click with glowstone to make statues illuminate, gunpowder to remove lighting, palette to colourize and paper to display nametag (Shouldn't be visible through walls)
@Suppress("UNUSED")
object StatuesClassic: ModInitializer {

    const val MODID = "statuesclassic"

    val LOGGER: Logger = LogManager.getLogger()
    val GROUP: ItemGroup = FabricItemGroupBuilder.build(Identifier(MODID, MODID)) { ItemStack(Items.PLAYER_HEAD) }

    override fun onInitialize() {
        val sTime = System.currentTimeMillis()
        LOGGER.info("Statues Classic initializing...")
        val sRegTime = System.currentTimeMillis()
        ModBlocks.regAll(Registry.BLOCK, Block::class, MODID)
        ModItems.regAll(Registry.ITEM, Item::class, MODID)

        ModBlockEntities.regAll(Registry.BLOCK_ENTITY_TYPE, BlockEntityType::class, MODID)

        val eRegTime = System.currentTimeMillis()

        ModScreenHandlers.wake()
        ServerPacketHandlers.register()

        val eTime = System.currentTimeMillis()
        LOGGER.info("Statues Classic initialization complete in ${eTime-sTime} milliseconds.")
    }
}