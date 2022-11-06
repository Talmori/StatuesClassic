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

import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.Dilation
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.PlayerEntityModel
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
        val ctx = EntityRendererFactory.Context(mc.entityRenderDispatcher, mc.itemRenderer, mc.blockRenderManager, mc.entityRenderDispatcher.heldItemRenderer, mc.resourceManager, mc.entityModelLoader, mc.textRenderer)

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