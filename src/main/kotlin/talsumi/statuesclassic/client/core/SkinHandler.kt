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
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.util.Identifier
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.Color
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.HashMap

object SkinHandler {

    private val cache = HashMap<UUID, SkinData>()
    private val texturedCache = HashMap<Pair<UUID, BlockState>, Identifier>()

    fun reset()
    {
        texturedCache.clear()
        cache.clear()
    }

    fun getTexturedSkin(uuid: UUID, block: BlockState): Identifier?
    {
        val key = Pair(uuid, block)
        if (!texturedCache.containsKey(key)) {
            val skin = makeTexturedSkin(uuid, block)
            if (skin != null)
                texturedCache[key] = skin
            return skin
        }
        else {
            return texturedCache[key]!!
        }
    }

    private fun makeTexturedSkin(uuid: UUID, block: BlockState): Identifier?
    {
        val sTime = System.currentTimeMillis()
        val baseSkin = getCachedSkin(uuid)?.skin ?: return null

        val mc = MinecraftClient.getInstance()
        val textures = mc.textureManager

        //Get block texture
        var width: Int
        var height: Int
        val blockImage = streamBlockTexture(block).use { ImageIO.read(it) }.let {
            width = it.width
            height = it.height
            it.getRGB(0, 0, it.width, it.height, null, 0, it.width * it.height)}

        //Read in skin texture
        textures.getTexture(baseSkin).bindTexture()
        val pixelsBuffer = BufferUtils.createIntBuffer(64 * 64)
        GL12.glGetTexImage(GL12.GL_TEXTURE_2D, 0, GL12.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelsBuffer)
        val skinPixels = IntArray(64 * 64)
        pixelsBuffer.get(skinPixels)

        val newImage = NativeImage(64, 64, true)

        for (bgra in skinPixels.withIndex()) {
            val x = (bgra.index % 64)
            val y = (bgra.index / 64)
            val blockY = y % 16
            val blockX = x % 16
            val blockIndex = (blockY * width) + blockX

            val skinPixel = Color(bgra.value, true).let {
                Color(it.red, it.green, it.blue, it.alpha)
            }

            Color(blockImage[blockIndex]).let {
                newImage.setColor(x, y, mixColors(skinPixel, it))
            }
        }

        /*val stream = object: InputStream() {
            var pos = 0
            var bytes = Array<Byte>(4) { 0 }
            var lastByte = 0

            override fun read(): Int
            {
                if (pos >= newTexturePixels.size)
                    return -1

                if (lastByte < 0) {
                    val int = newTexturePixels[pos++]
                    bytes[3] = (int shr 0).toByte()
                    bytes[2] = (int shr 8).toByte()
                    bytes[1] = (int shr 16).toByte()
                    bytes[0] = (int shr 24).toByte()
                    lastByte = 3
                }

                return bytes[lastByte--].toInt()
            }
        }*/

        val eTime = System.currentTimeMillis()
        return textures.registerDynamicTexture("statuesclassic_${uuid}_${UUID.randomUUID()}".lowercase(), NativeImageBackedTexture(newImage))
    }

    //TODO: This
    private fun mixColors(skin: Color, block: Color): Int
    {
        return skin.rgb

        val skinHSV = Color.RGBtoHSB(skin.red, skin.green, skin.blue, null)
        val blockHSV = Color.RGBtoHSB(block.red, block.green, block.blue, null)
        val out = Color(Color.HSBtoRGB((skinHSV[0] + blockHSV[0]) / 512f, skinHSV[1], (skinHSV[2] + blockHSV[2]) / 512f))
    }

    private fun streamBlockTexture(block: BlockState): InputStream?
    {
        val mc = MinecraftClient.getInstance()
        val sprite = mc.blockRenderManager.getModel(block).particleSprite
        return try {
            mc.resourceManager.getResource(Identifier(sprite.id.namespace, "textures/${sprite.id.path}.png")).inputStream
        } catch (e: Exception) {
            null
        }
    }
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