package talsumi.statuesclassic.client.core

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import java.util.*

object SkinHandler {

    private val skinCache = HashMap<UUID, Pair<Identifier, Boolean>>()
    private val capeCache = HashMap<UUID, Pair<Identifier, Boolean>>()
    private val elytraCache = HashMap<UUID, Pair<Identifier, Boolean>>()

    private fun getCache(type: MinecraftProfileTexture.Type) = when (type) {
        MinecraftProfileTexture.Type.SKIN -> skinCache
        MinecraftProfileTexture.Type.CAPE -> capeCache
        MinecraftProfileTexture.Type.ELYTRA -> elytraCache
    }

    /**
     * Returns the skin data for [uuid], whenever it is ready.
     * Calling will download data, when it is available this method will return it. Before it is available it will return null.
     */
    fun getCachedSkin(uuid: UUID, type: Type)
    {
        val cache = getCache(type)

        if (!cache.containsKey(uuid)) {

        }
    }

    //TODO: Slim skins
    fun getSkin(uuid: UUID, type: MinecraftProfileTexture.Type, whenReady: (Identifier, Boolean) -> Unit)
    {
        val cache = getCache(type)
        if (cache.containsKey(uuid)) {
            whenReady.invoke(cache[uuid]!!.first, cache[uuid]!!.second)
        }
        else {
            MinecraftClient.getInstance().skinProvider.loadSkin(
                GameProfile(uuid, null),
                { textureType, id, mcProfileTexture ->
                    run {
                        when (textureType) {
                            MinecraftProfileTexture.Type.SKIN -> skinCache[uuid] = Pair(id, false)
                            MinecraftProfileTexture.Type.CAPE -> capeCache[uuid] = Pair(id, false)
                            MinecraftProfileTexture.Type.ELYTRA -> elytraCache[uuid] = Pair(id, false)
                        }
                        if (textureType == type)
                            whenReady.invoke(id, false)
                        println(mcProfileTexture.getMetadata("slim"))
                    }
                }, true
            )
        }
    }
}