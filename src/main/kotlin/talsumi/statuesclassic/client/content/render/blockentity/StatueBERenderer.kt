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
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.texture.PlayerSkinProvider
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.content.blockentity.StatueBE
import java.util.*
import kotlin.collections.HashMap

class StatueBERenderer(): BlockEntityRenderer<StatueBE> {

    private val cache = HashMap<UUID, CacheModel>()

    private fun getOrCreateCachedModel(uuid: UUID): CacheModel
    {
        return cache[uuid] ?: run {
            val model = CacheModel(false, false, null)
            cache[uuid] = model
            dummy()
            model
        }
    }

    fun dummy()
    {
        println("Created model!")
    }

    override fun render(statue: StatueBE, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        if (!statue.hasBeenSetup)
            return

        val cache = getOrCreateCachedModel(statue.playerUuid!!)

        if (!cache.setup) {
            //TODO: Slim skins
            if (!cache.pending) {
                val uuid = statue.playerUuid
                SkinCache.getSkin(uuid!!, whenReady = { texture, slim -> setup(cache, statue, uuid, slim, texture)})
                cache.pending = true
            }
        }
        else {
            matrices.push()
            val model = cache.model!!
            val vertex = vertexProvider.getBuffer(RenderLayer.getEntityTranslucent(model.texture))

            matrices.translate(0.5, 1.5, 0.5)
            val facing = statue.cachedState.get(Properties.HORIZONTAL_FACING)
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(facing.asRotation()))
            if (facing == Direction.EAST || facing == Direction.WEST)
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f))

            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f))

            model.setAngles(statue)
            model.render(matrices, vertex, light, overlay, 1f, 1f, 1f, 1f)

            matrices.pop()
        }
    }

    fun setup(cache: CacheModel, statue: StatueBE, uuid: UUID, slim: Boolean, texture: Identifier)
    {
        cache.model = StatueModel(slim, texture)
        cache.setup = true
    }

    class CacheModel(var setup: Boolean, var pending: Boolean, var model: StatueModel?)
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