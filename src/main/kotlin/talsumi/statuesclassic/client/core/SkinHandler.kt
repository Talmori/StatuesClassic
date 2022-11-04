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
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL12
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.mixins.MinecraftClientAccessor
import java.awt.Color
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO

object SkinHandler {

    private val missingTexture = Identifier(StatuesClassic.MODID, "textures/block/missing.png")
    private val executor = Util.getMainWorkerExecutor()
    private val cache = HashMap<UUID, SkinData>()
    private val texturedCache = HashMap<Pair<UUID, BlockState>?, AsyncHolder>()
    private val baseUUID = UUID.randomUUID()

    private val processing = Executors.newCachedThreadPool()

    private val defaultSkinTexture = Identifier("textures/entity/steve.png")
    private val slimSkinTexture = Identifier("textures/entity/alex.png")

    fun reset()
    {
        texturedCache.clear()
        cache.clear()
    }

    /**
     * Creates and returns a skin for [uuid], textured to look like [block]. This will return null until the skin has been created.
     * If [uuid] is null, the skin is replaced by the default one.
     */
    fun getTexturedSkin(uuid: UUID?, block: BlockState): Identifier?
    {
        val key = uuid?.let { Pair(uuid, block) }

        return if (texturedCache.containsKey(key)) {
            texturedCache[key]?.tex
        } else {
            val holder = AsyncHolder(null)

            if (uuid == null) {
                makeTexturedSkin(DefaultSkinHelper.getTexture(), baseUUID, block, holder)
            } else {
                processing.execute {
                    var firstSleep = false

                    while (true) {
                        val skin = getCachedSkin(uuid).skin

                        if (skin == null) {
                            Thread.sleep(if (firstSleep) 20 else 250)
                            firstSleep = true
                        }
                        else {
                            val mc = MinecraftClient.getInstance() as MinecraftClientAccessor

                            //makeTexturedSkin needs to be run on the render thread.
                            mc.renderTaskQueue.add {
                                makeTexturedSkin(skin, uuid, block, holder)
                            }

                            break
                        }
                    }
                }
            }

            texturedCache[key] = holder
            holder.tex
        }

        /*if (uuid == null) {
            //If cache contains the key, the texture is either processing or created.
            if (texturedCache.containsKey(key)) {
                return texturedCache[key]?.tex
            }
            else {
                val holder = AsyncHolder(null)
                makeTexturedSkin(DefaultSkinHelper.getTexture(), baseUUID, block, holder)
                texturedCache[key] = holder
                return holder.tex
            }
        }
        else {
            return if (texturedCache.containsKey(key)) {
                texturedCache[key]?.tex
            } else {
                val holder = AsyncHolder(null)
                //This will fill [holder] with the skin when it is ready, some time in the future.
                getCachedSkin(uuid, Callback() { makeTexturedSkin(it.skin!!, baseUUID, block, holder) })
                texturedCache[key] = holder
                holder.tex
            }
        }*/
        /*val key = Pair(uuid ?: baseUUID, block)
        if (!texturedCache.containsKey(key)) {
            val holder = AsyncHolder(null)

            val base = (if (uuid != null) getCachedSkin(uuid)?.skin else DefaultSkinHelper.getTexture()) ?: return null
            makeTexturedSkin(base, uuid ?: baseUUID, block, holder)


            texturedCache[key] = holder
            return holder.tex
        }
        else {
            return texturedCache[key]!!.tex
        }*/
        /*
        val key = Pair(uuid ?: baseUUID, block)
        if (!texturedCache.containsKey(key)) {
            val data = if (uuid != null) makeTexturedSkin() getCachedSkin(uuid) else return null

            if (data)
            if (data.complete != null && uuid != null) {
                val holder = AsyncHolder(null)
                makeTexturedSkin(base, uuid, block, holder)
                texturedCache[key] = holder
                return holder.tex
            }

            return null
        }
        else {
            return texturedCache[key]!!.tex
        }
         */
    }

    //TODO: In the future, maybe make > 64x64 skins out of > 16x16 textures.
    private fun makeTexturedSkin(baseSkin: Identifier, uuid: UUID, block: BlockState, holder: AsyncHolder)
    {
        val sTime = System.currentTimeMillis()
        var baseSkin = baseSkin

        val mc = MinecraftClient.getInstance()
        val textures = mc.textureManager

        //Get block texture
        var width: Int
        var height: Int
        val blockImage = streamBlockTexture(block).use { ImageIO.read(it) }.let {
            width = it.width
            height = it.height
            it.getRGB(0, 0, it.width, it.height, null, 0, it.width)}
        val blockSize = height.coerceAtMost(width)
        val sampleResolution = 16.coerceAtMost(blockSize)

        //If we are using a default (steve/alex) skin, use our guaranteed 64x64 skin instead, or it will crash badly.
        when (baseSkin) {
            defaultSkinTexture -> baseSkin = TrueDefaultSkins.default_default_skin
            slimSkinTexture -> baseSkin = TrueDefaultSkins.default_slim_skin
        }

        //Read in skin texture
        textures.getTexture(baseSkin).bindTexture()
        val skinWidth = GL12.glGetTexLevelParameteri(GL12.GL_TEXTURE_2D, 0, GL12.GL_TEXTURE_WIDTH)
        val skinHeight = GL12.glGetTexLevelParameteri(GL12.GL_TEXTURE_2D, 0, GL12.GL_TEXTURE_HEIGHT)

        if (skinWidth != 64 || skinHeight != 64) {
            //Just in case a mod changes the texture locations for default skins (why?) and the skin isn't 64x64, fallback to our default skin.
            textures.getTexture(TrueDefaultSkins.default_default_skin).bindTexture()

            val skinWidth = GL12.glGetTexLevelParameteri(GL12.GL_TEXTURE_2D, 0, GL12.GL_TEXTURE_WIDTH)
            val skinHeight = GL12.glGetTexLevelParameteri(GL12.GL_TEXTURE_2D, 0, GL12.GL_TEXTURE_HEIGHT)

            //If it turns out the guaranteed 64x64 skin *isn't* guaranteed to be 64x64, just crash, or it will cause an exception_access_violation and probably not even leave a crash report!
            if (skinWidth != 64 || skinHeight != 64)
                throw IllegalStateException("Default skin dimensions [width=${skinWidth}, height=${skinHeight}] are not equal to [width=64, height=64]! Check the textures under 'assets/statuesclassic/textures/entity'!")
        }

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
                val blockX = (x % sampleResolution) * (blockSize / sampleResolution)
                val blockY = (y % sampleResolution) * (blockSize / sampleResolution)
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

    private fun streamBlockTexture(block: BlockState): InputStream
    {
        val mc = MinecraftClient.getInstance()
        val sprite = mc.blockRenderManager.getModel(block).particleSprite
        var resource: Resource? = null
        try {
            resource = mc.resourceManager.getResource(Identifier(sprite.id.namespace, "textures/${sprite.id.path}.png")).orElse(null)

            if (resource != null)
                return resource.inputStream

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        return (mc.resourceManager.getResource(missingTexture).orElseThrow{ IllegalStateException("Missing texture is missing! What?") }).inputStream
    }

    /**
     * Call to get a [SkinData] object. Its values will be null until they have been loaded by the game.
     * Note: [callback] is designed for use in situations where this method will only be called once, not repeatedly.
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
            val cached = cache[uuid]!!
            return cached
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

    object ReloadListener: SimpleSynchronousResourceReloadListener {
        val id = Identifier(StatuesClassic.MODID, "statue_skin_reload_listener")

        override fun reload(manager: ResourceManager?) = reset()

        override fun getFabricId(): Identifier = id
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

    class AsyncHolder(@Volatile var tex: Identifier?)


    class Callback(val function: (SkinData) -> Unit) {
        @Volatile var complete = false
        @Volatile var processing = false
        fun foundSkin(data: SkinData) = function.invoke(data)
    }
}