package talsumi.statuesclassic.client.content.model

import net.minecraft.client.model.Dilation
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3f
import talsumi.statuesclassic.core.StatueData


class StatueModel(val slim: Boolean) : PlayerEntityModel<AbstractClientPlayerEntity>(PlayerEntityModel.getTexturedModelData(Dilation.NONE, slim).root.createPart(64, 64), slim) {

    init
    {
        child = false
    }

    fun setAngles(data: StatueData)
    {
        //Head
        head.setAngles(data.headRaise, data.headRotate, 0f)
        hat.setAngles(data.headRaise, data.headRotate, 0f)
        //Left arm
        leftArm.setAngles(data.leftArmRaise, data.leftArmRotate, 0f)
        leftSleeve.setAngles(data.leftArmRaise, data.leftArmRotate, 0f)
        //Right arm
        rightArm.setAngles(data.rightArmRaise, data.rightArmRotate, 0f)
        rightSleeve.setAngles(data.rightArmRaise, data.rightArmRotate, 0f)
        //Left leg
        leftLeg.setAngles(data.leftLegRaise, data.leftLegRotate, 0f)
        leftPants.setAngles(data.leftLegRaise, data.leftLegRotate, 0f)
        //Right leg
        rightLeg.setAngles(data.rightLegRaise, data.rightLegRotate, 0f)
        rightPants.setAngles(data.rightLegRaise, data.rightLegRotate, 0f)
    }
}