package talsumi.statuesclassic.client.core

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.util.Identifier
import java.util.*

object SkinHandler {

    private val cache = HashMap<UUID, SkinData>()

    /**
     * Call to get a [SkinData] object. Its values will be null until they have been loaded by the game.
     */
    fun getCachedSkin(uuid: UUID): SkinData
    {
        if (!cache.containsKey(uuid)) {
            val data = SkinData(null, null, null, null)
            cache[uuid] = data

            loadSkin(uuid, Type.SKIN) { id -> data!!.skin = id }
            loadSkin(uuid, Type.CAPE) { id -> data!!.cape = id }
            loadSkin(uuid, Type.ELYTRA) { id -> data!!.elytra = id }
            data.slim = false //TODO: Slim skins

            return data
        }
        else {
            return cache[uuid]!!
        }
    }

    fun loadSkin(uuid: UUID, type: Type, whenReady: (Identifier) -> Unit)
    {
        MinecraftClient.getInstance().skinProvider.loadSkin(
            GameProfile(uuid, null),
            { textureType, id, mcProfileTexture ->
                run {
                    if (textureType == type)
                        whenReady.invoke(id)
                }
            }, true
        )
    }

    class SkinData(var skin: Identifier?, var cape: Identifier?, var elytra: Identifier?, var slim: Boolean?, private var complete: Boolean = false) {

        fun isComplete(): Boolean
        {
            return if (complete) true else {
                complete = skin != null && cape != null && elytra != null && slim != null
                complete
            }
        }

        /**
         * Returns the held skin, or if it hasn't been loaded yet, the default one.
         */
        fun getSkinOrDefault(): Identifier = skin ?:DefaultSkinHelper.getTexture()
    }
}