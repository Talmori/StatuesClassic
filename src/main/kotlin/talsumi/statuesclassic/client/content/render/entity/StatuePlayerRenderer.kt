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

package talsumi.statuesclassic.client.content.render.entity

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.PlayerEntityRenderer
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.ModelWithArms
import net.minecraft.client.render.entity.model.ModelWithHead
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.item.HeldItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.Arm
import net.minecraft.util.math.Vec3f
import net.minecraft.world.RaycastContext
import talsumi.statuesclassic.client.compat.SkippedFeatureRenderers
import talsumi.statuesclassic.client.content.entity.StatuePlayerEntity
import talsumi.statuesclassic.client.core.StatueModels
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueData
import talsumi.statuesclassic.core.StatueHelper
import talsumi.statuesclassic.mixins.StatuesClassicPlayerEntityRendererInvoker
import java.awt.Color
import java.util.*

abstract class StatuePlayerRenderer(ctx: EntityRendererFactory.Context?, slim: Boolean) : PlayerEntityRenderer(ctx, slim) {

    private val heldItemFeatureRendererOverride: StatueHeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>
    private val capeFeatureRendererOverride: StatueCapeFeatureRenderer

    init
    {
        val heldItemRenderer = MinecraftClient.getInstance().entityRenderDispatcher.heldItemRenderer
        heldItemFeatureRendererOverride = StatueHeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this, heldItemRenderer)
        capeFeatureRendererOverride = StatueCapeFeatureRenderer(this)
    }

    /**
     * Getter for supplying [FeatureRenderer]s with the proper context model.
     */
    //This method is defined in [StatueModels], as feature renderers are created before [model] is set, causing weirdness.
    abstract override fun getModel(): PlayerEntityModel<AbstractClientPlayerEntity>

    fun render(statue: StatueBE, data: StatueData, color: Color, tickDelta: Float, matrices: MatrixStack, vertex: VertexConsumer, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        matrices.push()
        val model = getModel()
        model.child = false
        val invoker = this as StatuesClassicPlayerEntityRendererInvoker
        val player = statue.clientFakePlayer as? AbstractClientPlayerEntity ?: return

        //StatueModels.setAngles(statueModel, data)
        //invoker.invokeSetModelPose(player)

        StatueModels.setAngles(getModel(), data)
        //StatueModels.setAngles(model, data)

        //Rotate to match statue facing
        val facing = statue.cachedState.get(Properties.HORIZONTAL_FACING)
        var rotate = facing.asRotation()
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotate))

        //Apply height offset
        matrices.translate(0.0, data.heightOffset.toDouble(), 0.0)

        //Apply master rotation
        matrices.translate(0.0, 1.0, 0.0)
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(data.masterRotate))
        matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(data.masterRaise))
        matrices.translate(0.0, -1.0, 0.0)

        matrices.push()
        model.render(matrices, vertex, light, overlay, (color.red / 255f), (color.green / 255f), (color.blue / 255f), color.alpha / 255f)
        matrices.pop()

        //Render features
        for (feature in features) {
            if (SkippedFeatureRenderers.isSkipped(feature.javaClass)) continue
            var feature = feature

            //Ignore vanilla's held item renderer and use our own that supports rotation
            if (feature is PlayerHeldItemFeatureRenderer) {
                feature = heldItemFeatureRendererOverride
                feature.rightRotation = StatueHelper.encodeHandRotation(statue.rightHandRotate)
                feature.leftRotation = StatueHelper.encodeHandRotation(statue.leftHandRotate)
            }
            //Use our own cape renderer. Like vanilla ours delegates to a AbstractClientPlayerEntity (DummyPlayerEntity)
            else if (feature is CapeFeatureRenderer) {
                feature = capeFeatureRendererOverride
            }

            matrices.push()
            feature.render(matrices, vertexProvider, light, player, 0f, 0f, tickDelta, 0f, player.headYaw, player.pitch)
            matrices.pop()
        }

        matrices.pop()

        //Render nametag if applicable
        if (statue.hasName && statue.clientFakePlayer is AbstractClientPlayerEntity) {
            val mc = MinecraftClient.getInstance()

            //Raycast so we don't render when behind blocks.
            val player = mc.player ?: return
            val cast = mc.world?.raycast(RaycastContext(
                player.getCameraPosVec(tickDelta),
                statue.clientFakePlayer?.pos?.add(0.0, 2.1, 0.0) ?: return,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                mc.player))

            if (cast?.blockPos == statue.pos.up() || cast?.blockPos == statue.pos.up(2)) {
                matrices.push()
                matrices.scale(1f, -1f, -1f)
                matrices.translate(0.0, -1.4, 0.0)
                matrices.translate(0.0, -data.heightOffset.toDouble(), 0.0)
                renderLabelIfPresent(statue.clientFakePlayer as AbstractClientPlayerEntity, Text.of(statue.playerName), matrices, vertexProvider, light)
                matrices.pop()
            }
        }
    }
}

class StatueHeldItemFeatureRenderer<T, M>(context: FeatureRendererContext<T, M>, private val heldItemRenderer: HeldItemRenderer) : PlayerHeldItemFeatureRenderer<T, M>(context, heldItemRenderer) where T: AbstractClientPlayerEntity, M: EntityModel<T>, M: ModelWithHead, M: ModelWithArms {

    var rightRotation = 0f
    var leftRotation = 0f

    override fun renderItem(entity: LivingEntity, stack: ItemStack, transformationMode: ModelTransformation.Mode, arm: Arm, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int)
    {
        if (!stack.isEmpty) {
            matrices.push()
            (this.contextModel as ModelWithArms).setArmAngle(arm, matrices)
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0f))
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f))
            val bl = arm == Arm.LEFT
            matrices.translate(((if (bl) -1 else 1).toFloat() / 16.0f).toDouble(), 0.0, 0.0) //Centre on arm
            matrices.translate(0.0, 0.0, -0.5) //Move down arm
            val rotation = if (arm == Arm.RIGHT) rightRotation else leftRotation

            matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(if (rotation < 0) rotation * 2 else rotation)) //Rotate item
            matrices.translate(0.0, 0.125, -0.125) //Centre item on rotation
            heldItemRenderer.renderItem(entity, stack, transformationMode, bl, matrices, vertexConsumers, light)
            matrices.pop()
        }
    }
}

class StatueCapeFeatureRenderer(context: FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) : CapeFeatureRenderer(context) {
    override fun render(matrices: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, light: Int, player: AbstractClientPlayerEntity, limbAngle: Float, limbDistance: Float, tickDelta: Float, animationProgress: Float, headYaw: Float, headPitch: Float)
    {
        if (player.canRenderCapeTexture() && player.isPartVisible(PlayerModelPart.CAPE) && player.capeTexture != null && !player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {

            matrices.push()
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f))
            matrices.translate(0.0, 0.0, -0.16)
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-8.0f))
            val vertexes = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(player.capeTexture))
            (this.contextModel as PlayerEntityModel<*>).renderCape(matrices, vertexes, light, OverlayTexture.DEFAULT_UV)
            matrices.pop()
        }
    }
}