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

            loadSkin(uuid, Type.SKIN) {
                    id, tex -> data!!.skin = id
                    data!!.slim = tex.getMetadata("model") == "slim"
            }
            loadSkin(uuid, Type.CAPE) { id, tex -> data!!.cape = id }
            loadSkin(uuid, Type.ELYTRA) { id, tex -> data!!.elytra = id }

            return data
        }
        else {
            return cache[uuid]!!
        }
    }

    fun loadSkin(uuid: UUID, type: Type, whenReady: (Identifier, MinecraftProfileTexture) -> Unit)
    {
        MinecraftClient.getInstance().skinProvider.loadSkin(
            GameProfile(uuid, null),
            { textureType, id, mcProfileTexture ->
                run {
                    if (textureType == type)
                        whenReady.invoke(id, mcProfileTexture)
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

        override fun toString(): String = "SkinData(skin=$skin, cape=$cape, elytra=$elytra, slim=$slim)"
    }
}