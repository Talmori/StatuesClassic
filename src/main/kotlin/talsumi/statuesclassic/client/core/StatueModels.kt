package talsumi.statuesclassic.client.core

import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.Dilation
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.model.PlayerEntityModel
import talsumi.marderlib.compat.MLCompatRendering
import talsumi.statuesclassic.client.content.entity.StatuePlayerEntity
import talsumi.statuesclassic.client.content.render.entity.StatuePlayerRenderer
import talsumi.statuesclassic.core.StatueData

object StatueModels {

    val model: PlayerEntityModel<StatuePlayerEntity>
    val slimModel: PlayerEntityModel<StatuePlayerEntity>
    val statueRenderer: StatuePlayerRenderer
    val slimStatueRenderer: StatuePlayerRenderer

    init
    {
        val mc = MinecraftClient.getInstance()
        val ctx = MLCompatRendering.makeEntityRendererContext()

        model = object: PlayerEntityModel<StatuePlayerEntity>(getTexturedModelData(Dilation.NONE, false).root.createPart(64, 64), false) {
            init {

            }
        }
        slimModel = object: PlayerEntityModel<StatuePlayerEntity>(getTexturedModelData(Dilation.NONE, true).root.createPart(64, 64), true) {
            init {

            }
        }

        statueRenderer = object: StatuePlayerRenderer(ctx, false) {
            override fun getModel(): PlayerEntityModel<AbstractClientPlayerEntity> = model
        }
        slimStatueRenderer = object: StatuePlayerRenderer(ctx, true) {
            override fun getModel(): PlayerEntityModel<AbstractClientPlayerEntity> = slimModel as PlayerEntityModel<AbstractClientPlayerEntity>
        }

        model.child = false
        slimModel.child = false
    }

    fun setAngles(model: PlayerEntityModel<*>, data: StatueData)
    {
        //Head
        model.head.setAngles(data.headRaise, data.headRotate, 0f)
        model.hat.setAngles(data.headRaise, data.headRotate, 0f)
        //Left arm
        model.leftArm.setAngles(data.leftArmRaise, data.leftArmRotate, 0f)
        model.leftSleeve.setAngles(data.leftArmRaise, data.leftArmRotate, 0f)
        //Right arm
        model.rightArm.setAngles(data.rightArmRaise, data.rightArmRotate, 0f)
        model.rightSleeve.setAngles(data.rightArmRaise, data.rightArmRotate, 0f)
        //Left leg
        model.leftLeg.setAngles(data.leftLegRaise, data.leftLegRotate, 0f)
        model.leftPants.setAngles(data.leftLegRaise, data.leftLegRotate, 0f)
        //Right leg
        model.rightLeg.setAngles(data.rightLegRaise, data.rightLegRotate, 0f)
        model.rightPants.setAngles(data.rightLegRaise, data.rightLegRotate, 0f)
    }
}