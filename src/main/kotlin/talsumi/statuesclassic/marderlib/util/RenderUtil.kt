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

package talsumi.statuesclassic.marderlib.util

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.Shader

object RenderUtil {

    fun getSnapshot(): Snapshot
    {
        return Snapshot(RenderSystem.getShaderTexture(0), RenderSystem.getShaderColor().copyOf(), RenderSystem.getShader())
    }

    class Snapshot(val tex: Int, val colour: FloatArray, val shader: Shader?) {
        fun restore()
        {
            if (shader != null)
                RenderSystem.setShader { shader }
            RenderSystem.setShaderTexture(0, tex)
            RenderSystem.setShaderColor(colour[0], colour[1], colour[2], colour[3])
        }
    }
}