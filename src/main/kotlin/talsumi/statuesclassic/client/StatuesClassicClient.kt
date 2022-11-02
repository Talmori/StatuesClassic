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

package talsumi.statuesclassic.client
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys
import net.minecraft.client.render.RenderLayer
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.ModClientScreens
import talsumi.statuesclassic.client.content.render.blockentity.StatueBERenderer
import talsumi.statuesclassic.client.core.SkinHandler
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.networking.ClientPacketHandlers
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Suppress("UNUSED")
object StatuesClassicClient: ClientModInitializer {

    var loads = 1

    override fun onInitializeClient()
    {
        ClientPacketHandlers.register()
        ModClientScreens.wake()
        BlockEntityRendererRegistry.register(ModBlockEntities.statue) {
            StatuesClassic.LOGGER.info("Created StatueBERenderer number ${loads++}")
            StatueBERenderer()
        }
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(SkinHandler.ReloadListener)
        registerBlockRenderLayers()
    }

    private fun registerBlockRenderLayers()
    {
        BlockRenderLayerMap.INSTANCE.putBlocks(
            RenderLayer.getTranslucent(),
            ModBlocks.statue_child,
            ModBlocks.statue_parent)
    }
}