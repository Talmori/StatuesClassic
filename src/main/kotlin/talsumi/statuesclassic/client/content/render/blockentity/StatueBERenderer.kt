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

package talsumi.statuesclassic.client.content.render.blockentity

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f
import talsumi.marderlib.util.RenderUtil
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.client.content.render.entity.StatuePlayerRenderer
import talsumi.statuesclassic.client.core.SkinHandler
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueData
import java.awt.Color
import java.util.*

class StatueBERenderer(): BlockEntityRenderer<StatueBE> {

    companion object {
        internal val model = StatueModel(false)
        internal val slimModel = StatueModel(true)
        internal val statueRenderer: StatuePlayerRenderer
        internal val slimStatueRenderer: StatuePlayerRenderer

        init
        {
            val mc = MinecraftClient.getInstance()
            val ctx = EntityRendererFactory.Context(mc.entityRenderDispatcher, mc.itemRenderer, mc.resourceManager, mc.entityModelLoader, mc.textRenderer)
            statueRenderer = StatuePlayerRenderer(model, ctx, false)
            slimStatueRenderer = StatuePlayerRenderer(slimModel, ctx, true)
        }
    }

    private fun internalRender(statue: StatueBE?, data: StatueData, model: StatueModel, color: Color, renderer: StatuePlayerRenderer, matrices: MatrixStack, vertex: VertexConsumer, vertexProvider: VertexConsumerProvider, tickDelta: Float, overlay: Int, light: Int)
    {
        matrices.push()

        //Flip so we aren't upside down
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f))

        //Apply master rotation
        matrices.translate(0.0, 1.0, 0.0)
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(data.masterRotate))
        matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(data.masterRaise))
        matrices.translate(0.0, -1.0, 0.0)

        //Apply rotations from data. This will carry through into a PlayerEntityRenderer render call
        model.setAngles(data)

        //Our AbstractClientPlayerEntity is a bit hacky and may not work with all mods, so don't crash if it doesn't!
        try {
            if (statue != null) {
                //If statue BlockEntity is present, render via a PlayerEntityRenderer. This allows armor, held items, etc.
                renderer.render(statue, color, tickDelta, matrices, vertex, vertexProvider, light, overlay)
            }
            else {
                //If BlockEntity isn't present, render directly from a model. Used for the statue creation GUI.
                model.render(matrices, vertex, light, overlay, color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            }
        } catch (e: Exception) {

        }

        matrices.pop()
    }

    /**
     * Renders a statue using [data] for rotations. [statue] may be null, in which case features (Armour, held items, etc.) cannot be rendered
     */
    fun render(statue: StatueBE?, data: StatueData, block: BlockState, uuid: UUID?, slim: Boolean, texture: Identifier, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        matrices.push()
        val snapshot = RenderUtil.getSnapshot()

        val model = if (slim) slimModel else model
        val renderer = if (slim) slimStatueRenderer else statueRenderer
        val colorized = statue?.isColoured ?: false
        val texture = (if (!colorized) SkinHandler.getTexturedSkin(uuid, block) else texture)
        val color = Color.WHITE//if (colorized) Color.WHITE else blockData.color

        if (texture != null) {
            val vertex = vertexProvider.getBuffer(RenderLayer.getEntityTranslucent(texture))

            //Render statue
            internalRender(statue, data, model, color, renderer, matrices, vertex, vertexProvider, tickDelta, overlay, light)
        }

        snapshot.restore()
        matrices.pop()
    }

    override fun render(statue: StatueBE, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        if (!statue.hasBeenSetup)
            return

        val cache = SkinHandler.getCachedSkin(statue.playerUuid ?: return)

        if (cache.skin != null) {
            matrices.push()
            matrices.translate(0.5, 1.5, 0.5)

            render(statue, statue.data!!, statue.block?.defaultState ?: Blocks.STONE.defaultState, statue.playerUuid!!, cache.slim!!, cache.skin!!, tickDelta, matrices, vertexProvider, light, overlay)
            matrices.pop()
        }
    }

    class CacheData(var setup: Boolean, var pending: Boolean, var slim: Boolean, var texture: Identifier?)
}