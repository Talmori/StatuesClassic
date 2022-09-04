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
import net.minecraft.util.Util
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL12
import talsumi.statuesclassic.StatuesClassic
import java.awt.Color
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import kotlin.collections.HashMap

object SkinHandler {

    private val executor = Util.getMainWorkerExecutor()
    private val cache = HashMap<UUID, SkinData>()
    private val texturedCache = HashMap<Pair<UUID, BlockState>, AsyncHolder>()
    private val baseUUID = UUID.randomUUID()

    fun reset()
    {
        texturedCache.clear()
        cache.clear()
    }

    /**
     * Creates and returns a skin for [uuid], textured to look like [block]. This will return null until the skin has been created.
     */
    fun getTexturedSkin(uuid: UUID?, block: BlockState): Identifier?
    {
        val key = Pair(uuid ?: baseUUID, block)
        if (!texturedCache.containsKey(key)) {
            val holder = AsyncHolder(null)
            val base = (if (uuid != null) getCachedSkin(uuid)?.skin else DefaultSkinHelper.getTexture()) ?: return null
            makeTexturedSkin(base, uuid ?: baseUUID, block, holder)
            texturedCache[key] = holder
            return holder.tex
        }
        else {
            return texturedCache[key]!!.tex
        }
    }

    private fun makeTexturedSkin(baseSkin: Identifier, uuid: UUID, block: BlockState, holder: AsyncHolder)
    {
        val sTime = System.currentTimeMillis()

        val mc = MinecraftClient.getInstance()
        val textures = mc.textureManager

        //Get block texture
        var width: Int
        var height: Int
        val blockImage = streamBlockTexture(block).use { ImageIO.read(it) }.let {
            width = it.width
            height = it.height
            it.getRGB(0, 0, it.width, it.height, null, 0, it.width.coerceAtLeast(it.height))}

        //Read in skin texture
        textures.getTexture(baseSkin).bindTexture()
        val pixelsBuffer = BufferUtils.createIntBuffer(64 * 64)
        GL12.glGetTexImage(GL12.GL_TEXTURE_2D, 0, GL12.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelsBuffer)
        val skinPixels = IntArray(64 * 64)
        pixelsBuffer.get(skinPixels)

        executor.execute {
            val newImage = NativeImage(64, 64, true)

            val skinArray = FloatArray(3)
            val blockArray = FloatArray(3)

            //Calculate the new colour of each pixel
            for (rgba in skinPixels.withIndex()) {
                val x = (rgba.index % 64)
                val y = (rgba.index / 64)
                val blockX = (x % 16) * (width / 16)
                val blockY = (y % 16) * (height / 16)
                val blockIndex = (blockY * width) + blockX

                newImage.setColor(x, y, mixColors(rgba.value, blockImage[blockIndex], skinArray, blockArray))
            }

            val eTime = System.currentTimeMillis()
            holder.tex = textures.registerDynamicTexture("statuesclassic_${uuid}_${UUID.randomUUID()}".lowercase(), NativeImageBackedTexture(newImage))
        }
    }

    /**
     * Mixes two skin and block pixels.
     * [skinArray] and [blockArray] are used to store intermediate values in. They can be omitted.
     */
    private fun mixColors(skin: Int, block: Int, skinArray: FloatArray? = null, blockArray: FloatArray? = null): Int
    {
        val skinHSV = Color.RGBtoHSB(skin and 0x00FF0000 shr 16, skin and 0x0000FF00 shr 8, skin and 0x00000000FF, skinArray)
        val blockHSV = Color.RGBtoHSB(block and 0x000000FF, block and 0x0000FF00 shr 8, block and 0x00FF0000 shr 16, blockArray)
        var sV = (blockHSV[2] * 0.2f + skinHSV[2] * 0.8f).coerceIn(0f, 1f)
        var bV = blockHSV[2]
        if (sV > bV)
            sV -= (sV-bV) / 2f

        val out = Color.HSBtoRGB(blockHSV[0], blockHSV[1], sV)
        return (out and 0x00FFFFFF) or (skin and 0xFF000000.toInt())
    }

    private fun streamBlockTexture(block: BlockState): InputStream?
    {
        val mc = MinecraftClient.getInstance()
        val sprite = mc.blockRenderManager.getModel(block).particleSprite
        return try {
            mc.resourceManager.getResource(Identifier(sprite.id.namespace, "textures/${sprite.id.path}.png")).inputStream
        } catch (e: Exception) {
            e.printStackTrace()
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
                    id, tex ->
                data!!.skin = id
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

    class SkinData(@Volatile var skin: Identifier?, @Volatile var cape: Identifier?, @Volatile var elytra: Identifier?, @Volatile var slim: Boolean?, private var complete: Boolean = false) {

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
        fun getSkinOrDefault(): Identifier = skin ?: DefaultSkinHelper.getTexture()

        override fun toString(): String = "SkinData(skin=$skin, cape=$cape, elytra=$elytra, slim=$slim)"
    }

    class AsyncHolder(var tex: Identifier?)
}