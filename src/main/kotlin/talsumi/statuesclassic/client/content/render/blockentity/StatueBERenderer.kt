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
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f
import talsumi.marderlib.util.RenderUtil
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.mixins.StatuesClassicArmorFeatureRendererInvoker
import java.util.*

class StatueBERenderer(): BlockEntityRenderer<StatueBE> {

    companion object {
        internal val model = StatueModel(false)
        internal val slimModel = StatueModel(true)

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

    //TODO: Shader for colourizing statues by base block
    /**
     * Renders a statue using [data] for rotations. [statue] may be null, in which case armour cannot be rendered
     */
    fun render(statue: StatueBE?, data: StatueData, uuid: UUID, slim: Boolean, texture: Identifier, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        matrices.push()
        val snapshot = RenderUtil.getSnapshot()

        val model = if (slim) slimModel else model
        val vertex = vertexProvider.getBuffer(RenderLayer.getEntityTranslucent(texture))

        //Flip so we aren't upside down
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f))

        //Apply master rotation
        matrices.translate(0.0, 1.0, 0.0)
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(data.masterRotate))
        matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(data.masterRaise))
        matrices.translate(0.0, -1.0, 0.0)

        //Apply rotations from data and render
        model.setAngles(data)
        model.render(matrices, vertex, light, overlay, 1f, 1f, 1f, 1f)

        //TODO: Rendering for ArmorRenderers
        //Render armour & held items
        if (statue != null) {
            model.getRenderer().renderAllArmour(statue, slim, tickDelta, matrices, vertexProvider, light, overlay, model)
            renderHands(statue, tickDelta, matrices, vertexProvider, light, overlay)
        }

        snapshot.restore()
        matrices.pop()
    }

    override fun render(statue: StatueBE, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        if (!statue.hasBeenSetup)
            return

        val cache = getCachedData(statue.playerUuid!!)

        if (cache != null) {
            matrices.push()
            matrices.translate(0.5, 1.5, 0.5)
            val facing = statue.cachedState.get(Properties.HORIZONTAL_FACING)
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(facing.asRotation()))
            render(statue, statue.data!!, statue.playerUuid!!, cache.slim, cache.texture!!, tickDelta, matrices, vertexProvider, light, overlay)
            matrices.pop()
        }
    }

    private fun renderHands(statue: StatueBE, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        renderItem(statue.inventory.rawGet(4), statue.leftHandRotate, tickDelta, matrices, vertexProvider, light, overlay)
        renderItem(statue.inventory.rawGet(5), statue.rightHandRotate, tickDelta, matrices, vertexProvider, light, overlay)
    }

    private fun renderItem(item: ItemStack, rotation: Float, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        if (!item.isEmpty)
            MinecraftClient.getInstance().itemRenderer.renderItem(item, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexProvider, 0)
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

class StatueArmourFeatureRenderer(private val leggings: BipedEntityModel<PlayerEntity>, val body: BipedEntityModel<PlayerEntity>): ArmorFeatureRenderer<PlayerEntity, BipedEntityModel<PlayerEntity>, BipedEntityModel<PlayerEntity>>(Context, leggings, body) {

    object Context: FeatureRendererContext<PlayerEntity, BipedEntityModel<PlayerEntity>> {
        private val ourModel = StatueModel(false)
        override fun getModel(): StatueModel = ourModel
        override fun getTexture(entity: PlayerEntity?): Identifier = DefaultSkinHelper.getTexture()
    }

    companion object {
        fun make(slim: Boolean): StatueArmourFeatureRenderer
        {
            val modelLoader = MinecraftClient.getInstance().entityModelLoader
            val armourInnerSlim = BipedEntityModel<PlayerEntity>(modelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM_INNER_ARMOR))
            val armourInnerStandard = BipedEntityModel<PlayerEntity>(modelLoader.getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR))
            val armourOuterSlim = BipedEntityModel<PlayerEntity>(modelLoader.getModelPart(EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR))
            val armourOuterStandard = BipedEntityModel<PlayerEntity>(modelLoader.getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR))
            return if (slim) StatueArmourFeatureRenderer(armourInnerSlim, armourOuterSlim) else StatueArmourFeatureRenderer(armourInnerStandard, armourOuterStandard)
        }
    }

    internal fun renderAllArmour(statue: StatueBE, slim: Boolean, tickDelta: Float, matrices: MatrixStack, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int, model: StatueModel)
    {
        renderSingleArmourPiece(statue.inventory.rawGet(0), EquipmentSlot.HEAD, matrices, vertexProvider, light, model, body)
        renderSingleArmourPiece(statue.inventory.rawGet(1), EquipmentSlot.CHEST, matrices, vertexProvider, light, model, body)
        renderSingleArmourPiece(statue.inventory.rawGet(2), EquipmentSlot.LEGS, matrices, vertexProvider, light, model, leggings)
        renderSingleArmourPiece(statue.inventory.rawGet(3), EquipmentSlot.FEET, matrices, vertexProvider, light, model, body)
    }

    private fun renderSingleArmourPiece(stack: ItemStack, slot: EquipmentSlot, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, model: StatueModel, renderModel: BipedEntityModel<PlayerEntity>)
    {
        this as StatuesClassicArmorFeatureRendererInvoker
        val item = stack.item

        if (item is ArmorItem) {
            if (item.slotType == slot) {
                model.setAttributes(renderModel)
                val isLegs = slot == EquipmentSlot.LEGS
                val hasEnchantmentGlint = item.hasGlint(stack)
                statuesclassic_invokeSetVisible(renderModel, slot)
                if (item is DyeableArmorItem) {
                    val i = item.getColor(stack)
                    val f = (i shr 16 and 255).toFloat() / 255.0f
                    val g = (i shr 8 and 255).toFloat() / 255.0f
                    val h = (i and 255).toFloat() / 255.0f
                    statuesclassic_invokeRenderArmorParts(matrices, vertexConsumers, light, item, hasEnchantmentGlint, renderModel, isLegs, f, g, h, null)
                    statuesclassic_invokeRenderArmorParts(matrices, vertexConsumers, light, item, hasEnchantmentGlint, renderModel, isLegs, 1.0f, 1.0f, 1.0f, "overlay")
                } else {
                    statuesclassic_invokeRenderArmorParts(matrices, vertexConsumers, light, item, hasEnchantmentGlint, renderModel, isLegs, 1.0f, 1.0f, 1.0f, null)
                }
            }
        }
    }


}