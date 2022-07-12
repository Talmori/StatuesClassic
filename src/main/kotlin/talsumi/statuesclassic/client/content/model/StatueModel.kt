package talsumi.statuesclassic.client.content.model

import net.minecraft.client.model.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import talsumi.statuesclassic.content.blockentity.StatueBE
import java.util.function.Function

class StatueModel(val slim: Boolean, val username: String, layerFactory: Function<Identifier, RenderLayer>?) : Model(layerFactory) {

    val head: ModelPart
    val hat: ModelPart
    val body: ModelPart
    val rightArm: ModelPart
    val leftArm: ModelPart
    val rightLeg: ModelPart
    val leftLeg: ModelPart
    val leftSleeve: ModelPart
    val rightSleeve: ModelPart
    val leftPants: ModelPart
    val rightPants: ModelPart
    val jacket: ModelPart
    val cloak: ModelPart
    val ear: ModelPart

    init
    {
        val dilation = Dilation(0f)
        val pivotOffsetY = 0f
        val modelData = ModelData()
        val modelPartData = modelData.root
        modelPartData.addChild(
            "head",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
            ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f)
        )
        modelPartData.addChild(
            "hat",
            ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
            ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f)
        )
        modelPartData.addChild(
            "body",
            ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
            ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f)
        )
        modelPartData.addChild(
            "ear",
            ModelPartBuilder.create().uv(24, 0).cuboid(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f),
            ModelTransform.NONE
        )
        modelPartData.addChild(
            "cloak",
            ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, dilation, 1.0f, 0.5f),
            ModelTransform.pivot(0.0f, 0.0f, 0.0f)
        )
        if (slim) {
            modelPartData.addChild(
                "left_arm",
                ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation),
                ModelTransform.pivot(5.0f, 2.5f, 0.0f)
            )
            modelPartData.addChild(
                "right_arm",
                ModelPartBuilder.create().uv(40, 16).cuboid(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation),
                ModelTransform.pivot(-5.0f, 2.5f, 0.0f)
            )
            modelPartData.addChild(
                "left_sleeve",
                ModelPartBuilder.create().uv(48, 48)
                    .cuboid(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation.add(0.25f)),
                ModelTransform.pivot(5.0f, 2.5f, 0.0f)
            )
            modelPartData.addChild(
                "right_sleeve",
                ModelPartBuilder.create().uv(40, 32)
                    .cuboid(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, dilation.add(0.25f)),
                ModelTransform.pivot(-5.0f, 2.5f, 0.0f)
            )
        } else {
            modelPartData.addChild(
                "left_arm",
                ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation),
                ModelTransform.pivot(5.0f, 2.0f, 0.0f)
            )
            modelPartData.addChild(
                "left_sleeve",
                ModelPartBuilder.create().uv(48, 48)
                    .cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)),
                ModelTransform.pivot(5.0f, 2.0f, 0.0f)
            )
            modelPartData.addChild(
                "right_sleeve",
                ModelPartBuilder.create().uv(40, 32)
                    .cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)),
                ModelTransform.pivot(-5.0f, 2.0f, 0.0f)
            )
        }
        modelPartData.addChild(
            "left_leg",
            ModelPartBuilder.create().uv(16, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation),
            ModelTransform.pivot(1.9f, 12.0f, 0.0f)
        )
        modelPartData.addChild(
            "left_pants",
            ModelPartBuilder.create().uv(0, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)),
            ModelTransform.pivot(1.9f, 12.0f, 0.0f)
        )
        modelPartData.addChild(
            "right_pants",
            ModelPartBuilder.create().uv(0, 32).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation.add(0.25f)),
            ModelTransform.pivot(-1.9f, 12.0f, 0.0f)
        )
        modelPartData.addChild(
            "jacket",
            ModelPartBuilder.create().uv(16, 32).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation.add(0.25f)),
            ModelTransform.NONE
        )

        head = modelPartData.getChild("head").createPart(64, 64)
        hat = modelPartData.getChild("hat").createPart(64, 64)
        body = modelPartData.getChild("body").createPart(64, 64)
        rightArm = modelPartData.getChild("right_arm").createPart(64, 64)
        leftArm = modelPartData.getChild("left_arm").createPart(64, 64)
        rightLeg = modelPartData.getChild("right_leg").createPart(64, 64)
        leftLeg = modelPartData.getChild("left_leg").createPart(64, 64)

        leftSleeve = modelPartData.getChild("left_sleeve").createPart(64, 64)
        rightSleeve = modelPartData.getChild("right_sleeve").createPart(64, 64)
        leftPants = modelPartData.getChild("left_pants").createPart(64, 64)
        rightPants = modelPartData.getChild("right_pants").createPart(64, 64)
        jacket = modelPartData.getChild("jacket").createPart(64, 64)
        cloak = modelPartData.getChild("cloak").createPart(64, 64)
        ear = modelPartData.getChild("ear").createPart(64, 64)
    }

    fun setAngles(statue: StatueBE)
    {
        head.setAngles(statue.headRaise, statue.headRotate, 0f)
    }

    override fun render(matrices: MatrixStack, vertices: VertexConsumer, light: Int, overlay: Int, red: Float, green: Float, blue: Float, alpha: Float)
    {
        head.render(matrices, vertices, light, overlay)
        hat.render(matrices, vertices, light, overlay)
        body.render(matrices, vertices, light, overlay)
        rightArm.render(matrices, vertices, light, overlay)
        leftArm.render(matrices, vertices, light, overlay)
        rightLeg.render(matrices, vertices, light, overlay)
        leftLeg.render(matrices, vertices, light, overlay)
        leftSleeve.render(matrices, vertices, light, overlay)
        rightSleeve.render(matrices, vertices, light, overlay)
        leftPants.render(matrices, vertices, light, overlay)
        rightPants.render(matrices, vertices, light, overlay)
        jacket.render(matrices, vertices, light, overlay)
        cloak.render(matrices, vertices, light, overlay)
        ear.render(matrices, vertices, light, overlay)
    }
}