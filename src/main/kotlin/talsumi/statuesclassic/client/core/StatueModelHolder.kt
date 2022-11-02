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
import net.minecraft.client.render.entity.EntityRendererFactory
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.client.content.model.StatueModel
import talsumi.statuesclassic.client.content.render.entity.StatuePlayerRenderer

object StatueModelHolder {

    val model = StatueModel(false)
    val slimModel = StatueModel(true)
    val statueRenderer: StatuePlayerRenderer
    val slimStatueRenderer: StatuePlayerRenderer

    init
    {
        StatuesClassic.LOGGER.info("Starting renderer creation!")
        val mc = MinecraftClient.getInstance()
        val ctx = EntityRendererFactory.Context(mc.entityRenderDispatcher, mc.itemRenderer, mc.blockRenderManager, mc.entityRenderDispatcher.heldItemRenderer, mc.resourceManager, mc.entityModelLoader, mc.textRenderer)
        StatuesClassic.LOGGER.info("Context created!")
        statueRenderer = StatuePlayerRenderer(model, ctx, false)
        StatuesClassic.LOGGER.info("Default renderer created!")
        slimStatueRenderer = StatuePlayerRenderer(slimModel, ctx, true)
        StatuesClassic.LOGGER.info("Slim renderer created!")
    }
}