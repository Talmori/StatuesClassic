package talsumi.statuesclassic.client.content.render.entity

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.PlayerEntityRenderer
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.ModelWithArms
import net.minecraft.client.render.entity.model.ModelWithHead
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.item.HeldItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Arm
import net.minecraft.util.math.Vec3f
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueCreation
import java.util.*

class StatuePlayerRenderer(val statueModel: StatueModel, ctx: EntityRendererFactory.Context?, slim: Boolean) : PlayerEntityRenderer(ctx, slim) {

    val heldItemFeatureRendererOverride = StatueHeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>(this)

    /**
     * Getter for supplying [FeatureRenderer]s with the proper context model.
     */
    override fun getModel(): PlayerEntityModel<AbstractClientPlayerEntity> = statueModel

    fun render(statue: StatueBE?, tickDelta: Float, matrices: MatrixStack, vertex: VertexConsumer, vertexProvider: VertexConsumerProvider, light: Int, overlay: Int)
    {
        statueModel.render(matrices, vertex, light, overlay, 1f, 1f, 1f, 1f)

        //Render armour & held items
        if (statue != null) {
            val player = statue.clientFakePlayer ?: return
            if (player !is AbstractClientPlayerEntity) return

            for (feature in features) {
                if (feature is CapeFeatureRenderer) continue
                var feature = feature

                //Ignore vanilla's held item renderer and use our own that supports rotation
                if (feature is PlayerHeldItemFeatureRenderer) {
                    feature = heldItemFeatureRendererOverride
                    feature.rightRotation = StatueCreation.encodeHandRotation(statue.rightHandRotate)
                    feature.leftRotation = StatueCreation.encodeHandRotation(statue.leftHandRotate)
                }

                feature.render(matrices, vertexProvider, light, player, 0f, 0f, tickDelta, 0f, player.headYaw, player.pitch)
            }
        }
    }
}

class StatueHeldItemFeatureRenderer<T, M>(context: FeatureRendererContext<T, M>) : PlayerHeldItemFeatureRenderer<T, M>(context) where T: AbstractClientPlayerEntity, M: EntityModel<T>, M: ModelWithHead, M: ModelWithArms {

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
            matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(if (arm == Arm.RIGHT) rightRotation else leftRotation)) //Rotate item
            matrices.translate(0.0, 0.125, -0.125) //Centre item on rotation
            MinecraftClient.getInstance().heldItemRenderer.renderItem(entity, stack, transformationMode, bl, matrices, vertexConsumers, light)
            matrices.pop()
        }
    }
}