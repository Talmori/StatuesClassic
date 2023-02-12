package talsumi.statuesclassic.client
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.render.RenderLayer
import net.minecraft.resource.ResourceType
import talsumi.statuesclassic.client.content.ModClientScreens
import talsumi.statuesclassic.client.content.render.blockentity.StatueBERenderer
import talsumi.statuesclassic.client.core.SkinHandler
import talsumi.statuesclassic.content.ModBlockEntities
import talsumi.statuesclassic.content.ModBlocks
import talsumi.statuesclassic.networking.ClientPacketHandlers

@Suppress("UNUSED")
object StatuesClassicClient: ClientModInitializer {

    override fun onInitializeClient()
    {
        ClientPacketHandlers.register()
        ModClientScreens.wake()
        BlockEntityRendererRegistry.register(ModBlockEntities.statue) { StatueBERenderer() }
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