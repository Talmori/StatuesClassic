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

package talsumi.statuesclassic.client.content.render.blockentity

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Shader
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.marderlib.util.RenderUtil
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap

class StatueBERenderer(): BlockEntityRenderer<StatueBE> {

    companion object {
        private val cache = HashMap<UUID, CacheData>()

        private fun getOrCreateCachedModel(uuid: UUID): CacheData
        {
            return cache[uuid] ?: run {
                val model = CacheData(false, false, false, null)
                cache[uuid] = model
                model
            }
        }

        private fun setupCachedData(cache: CacheData, slim: Boolean, texture: Identifier)
        {
            cache.slim = slim
            cache.texture = texture
            cache.setup = true
            cache.pending = false
        }

        /**
         * Returns the skin data for [uuid], whenever it is ready.
         * Calling will download data, when it is available this method will return it. Before it is available it will return null.
         */
        fun getCachedData(uuid: UUID): CacheData?
        {
            val cache = getOrCreateCachedModel(uuid)
            if (!cache.setup) {
                if (!cache.pending) {
                    SkinCache.getSkin(uuid!!, whenReady = { texture, slim -> setupCachedData(cache, slim, texture) })
                    cache.pending = true
                }

                return null
            }
            else
                return cache
        }
    }

    private val model = StatueModel(false)
    private val slimModel = StatueModel(true)

    //TODO: Shader for colourizing statues by base block
    fun render(data: StatueData, uuid: UUID, slim: Boolean, texture: Identifier, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int, overrideVC: VertexConsumer? = null)
    {
        matrices.push()
        val snapshot = RenderUtil.getSnapshot()

        val color = Color.MAGENTA
        val model = if (slim) slimModel else model
        val vertex = overrideVC ?: vertexProvider.getBuffer(RenderLayer.getEntityTranslucent(texture))

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f))

        model.setAngles(data)
        model.render(matrices, vertex, light, overlay, 1f, 1f, 1f, 1f)

        snapshot.restore()
        matrices.pop()
    }

    override fun render(statue: StatueBE, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        if (!statue.hasBeenSetup)
            return

        val cache = getCachedData(statue.playerUuid!!)
        /*val cache = getOrCreateCachedModel(statue.playerUuid!!)

        if (!cache.setup) {
            //TODO: Slim skins
            if (!cache.pending) {
                val uuid = statue.playerUuid
                SkinCache.getSkin(uuid!!, whenReady = { texture, slim -> setup(cache, statue, uuid, slim, texture)})
                cache.pending = true
            }
        }
        else {*/
        if (cache != null) {
            matrices.push()
            matrices.translate(0.5, 1.5, 0.5)
            val facing = statue.cachedState.get(Properties.HORIZONTAL_FACING)
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(facing.asRotation()))
            render(statue.data!!, statue.playerUuid!!, cache.slim, cache.texture!!, tickDelta, matrices, vertexProvider, light, overlay)
            matrices.pop()
        }
        //}
    }

    class CacheData(var setup: Boolean, var pending: Boolean, var slim: Boolean, var texture: Identifier?)
}

object SkinCache {

    private val cache = HashMap<UUID, Pair<Identifier, Boolean>>()

    fun getSkin(uuid: UUID, whenReady: (Identifier, Boolean) -> Unit)
    {
        if (cache.containsKey(uuid)) {
            whenReady.invoke(cache[uuid]!!.first, cache[uuid]!!.second)
        }
        else {
            MinecraftClient.getInstance().skinProvider.loadSkin(GameProfile(uuid, null),
                { type, id, texture -> whenReady.invoke(id, false); cache[uuid] = Pair(id, false)}, true
            )
        }
    }
}