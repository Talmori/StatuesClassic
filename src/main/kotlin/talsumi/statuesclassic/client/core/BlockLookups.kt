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

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import java.awt.Color
import javax.imageio.ImageIO

object BlockLookups {

    private val cache = HashMap<BlockState, Data>()

    fun getBlockColour(block: BlockState): Data
    {
        if (cache.containsKey(block)) {
            return cache[block]!!
        }
        else {
            val mc = MinecraftClient.getInstance()

            val sprite = mc.blockRenderManager.getModel(block).particleSprite
            val resource = mc.resourceManager.getResource(Identifier(sprite.id.namespace, "textures/${sprite.id.path}.png"))

            resource.inputStream.use {
                val image = ImageIO.read(it)

                val width = image.width
                val height = image.height
                val pixels = image.getRGB(0, 0, width, height, null, 0, width)
                val pixelCount = width * height

                var reds = 0
                var greens = 0
                var blues = 0

                //TODO: Speed this up
                for (pixel in pixels) {
                    val col = Color(pixel)
                    reds += col.red
                    greens += col.green
                    blues += col.blue
                }

                val data = Data(Color(reds / pixelCount, greens / pixelCount, blues / pixelCount),
                    Identifier(sprite.id.namespace, "textures/${sprite.id.path}.png"))
                cache[block] = data
                return data
            }
        }
    }

    class Data(val color: Color, val texture: Identifier)
}