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

package talsumi.statuesclassic.client.content.model

import net.minecraft.client.model.Dilation
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.entity.player.PlayerEntity
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
        //Master

    }
}